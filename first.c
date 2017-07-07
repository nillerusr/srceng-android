/*
 * Copyright (C) 2015 Mittorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include <string.h>
#include <stdio.h>
#include <jni.h>
//#include <sys/ucontext.h>
#include <asm/sigcontext.h>       /* for sigcontext */
#include <asm/signal.h>           /* for stack_t */
#include <signal.h>
#include <dlfcn.h>
#include <android/log.h>

#define TAG "HL2WRAPPER"


// IDIV emulation (broken)
#define ARM_OPCODE_CONDITION_UNCOND 0xf
#define ARM_OPCODE_CONDTEST_FAIL   0
#define ARM_OPCODE_CONDTEST_PASS   1
#define ARM_OPCODE_CONDTEST_UNCOND 2
/*
 * condition code lookup table
 * index into the table is test code: EQ, NE, ... LT, GT, AL, NV
 *
 * bit position in short is condition code: NZCV
 */

static const unsigned short cc_map[16] = {
 0xF0F0,        /* EQ == Z set            */
 0x0F0F,        /* NE                     */
 0xCCCC,        /* CS == C set            */
 0x3333,        /* CC                     */
 0xFF00,        /* MI == N set            */
 0x00FF,        /* PL                     */
 0xAAAA,        /* VS == V set            */
 0x5555,        /* VC                     */
 0x0C0C,        /* HI == C set && Z clear */
 0xF3F3,        /* LS == C clear || Z set */
 0xAA55,        /* GE == (N==V)           */
 0x55AA,        /* LT == (N!=V)           */
 0x0A05,        /* GT == (!Z && (N==V))   */
 0xF5FA,        /* LE == (Z || (N!=V))    */
 0xFFFF,        /* AL always              */
 0         /* NV                     */
};

/*
 * Returns:
 * ARM_OPCODE_CONDTEST_FAIL   - if condition fails
 * ARM_OPCODE_CONDTEST_PASS   - if condition passes (including AL)
 * ARM_OPCODE_CONDTEST_UNCOND - if NV condition, or separate unconditional
 *                              opcode space from v5 onwards
 *
 * Code that tests whether a conditional instruction would pass its condition
 * check should check that return value == ARM_OPCODE_CONDTEST_PASS.
 *
 * Code that tests if a condition means that the instruction would be executed
 * (regardless of conditional or unconditional) should instead check that the
 * return value != ARM_OPCODE_CONDTEST_FAIL.
 */

// Thumb mode bit
#define T_BIT           0x20

void sigtrap_handler(int signo, siginfo_t *si, void *data)
{

}
// Based on kernel function
inline unsigned int arm_check_condition(unsigned int opcode, unsigned int psr)
{
	unsigned int cc_bits  = opcode >> 28;
	unsigned int psr_cond = psr >> 28;
	unsigned int ret;

	if( cc_bits != ARM_OPCODE_CONDITION_UNCOND )
	{
		if ((cc_map[cc_bits] >> (psr_cond)) & 1)
			ret = ARM_OPCODE_CONDTEST_PASS;
		else
			ret = ARM_OPCODE_CONDTEST_FAIL;
	}
	else
		ret = ARM_OPCODE_CONDTEST_UNCOND;

	return ret;
}


typedef struct my_ucontext {
	unsigned long uc_flags;
	struct my_ucontext *uc_link;
	stack_t uc_stack;
	struct sigcontext uc_mcontext;
	unsigned long uc_sigmask;
} my_ucontext_t;

#define IS_THUMB(cpsr) ((cpsr) & 0x20)
#define REG_NUM(instr, offset) \
(((instr) & (0xf << (offset))) >> (offset))
#define EXTRACT_REG(num,cont) \
(*(&cont.arm_r0 + num))
#define DIVIDEND(cont) \
IS_THUMB(cont.arm_cpsr)?EXTRACT_REG(REG_NUM(*((unsigned int *)cont.arm_pc),16),cont):EXTRACT_REG(REG_NUM(*((unsigned int *)cont.arm_pc),0),cont)
#define DIVIDEND_NUM(cont) \
IS_THUMB(cont.arm_cpsr)?REG_NUM(*((unsigned int *)cont.arm_pc),16):REG_NUM(*((unsigned int *)cont.arm_pc),0)
#define DIVISOR(cont) \
IS_THUMB(cont.arm_cpsr)?EXTRACT_REG(REG_NUM(*((unsigned int *)cont.arm_pc),0),cont):EXTRACT_REG(REG_NUM(*((unsigned int *)cont.arm_pc),8),cont)
#define DIVISOR_NUM(cont) \
IS_THUMB(cont.arm_cpsr)?REG_NUM(*((unsigned int *)cont.arm_pc),0):REG_NUM(*((unsigned int *)cont.arm_pc),8)
#define DESTINATION(cont, value) \
if(IS_THUMB(cont.arm_cpsr))EXTRACT_REG(REG_NUM(*((unsigned int *)cont.arm_pc),8),cont)=value;else EXTRACT_REG(REG_NUM(*((unsigned int *)cont.arm_pc),16),cont)=value;
#define DESTINATION_NUM(cont) \
IS_THUMB(cont.arm_cpsr)?REG_NUM(*((unsigned int *)cont.arm_pc),8):REG_NUM(*((unsigned int *)cont.arm_pc),16)

void sighandler( int signo, siginfo_t *si, void *data )
{
	ucontext_t *uc = (ucontext_t *)data;

	unsigned int instr= *(unsigned int *)uc->uc_mcontext.arm_pc;

	//TODO: parse instruction at *arm_pc and emulate it

	if(((instr & 0x0310f010) == 0x0310f010)) { // idiv instructions

	//int res = arm_check_condition(instr, uc->uc_mcontext.arm_cpsr);
	if(!(DIVISOR(uc->uc_mcontext)))
	{
		struct sigaction sa;
		sigaction(SIGFPE, NULL, &sa);
		si->si_code = FPE_INTDIV;
		si->si_signo = SIGFPE;
		si->si_errno = 0;
		if(sa.sa_sigaction)sa.sa_sigaction(SIGFPE,si,data);
		else raise(SIGFPE);
		uc->uc_mcontext.arm_pc += 4;
		return;
	}
	//     if ((res == ARM_OPCODE_CONDTEST_PASS) || (res == ARM_OPCODE_CONDTEST_UNCOND) ) { // Check condition flags

		//__android_log_print(ANDROID_LOG_INFO, TAG, "Performing division r%d=%d by r%d=%d to r%d\n", DIVIDEND_NUM(uc->uc_mcontext), DIVIDEND(uc->uc_mcontext), DIVISOR_NUM(uc->uc_mcontext), DIVISOR(uc->uc_mcontext),DESTINATION_NUM(uc->uc_mcontext));
		if(instr &(1 << 21)) {//unsigned
		   DESTINATION(uc->uc_mcontext,(unsigned int)((unsigned int)(DIVIDEND(uc->uc_mcontext)))/((unsigned int)(DIVISOR(uc->uc_mcontext))));
		} else {
		   DESTINATION(uc->uc_mcontext,(int)((int)(DIVIDEND(uc->uc_mcontext)))/((int)(DIVISOR(uc->uc_mcontext))));
		}
	//}
	//     else    __android_log_print(ANDROID_LOG_INFO, TAG, "skipping uncond division, pc=0x%x, %x\n", uc->uc_mcontext.arm_pc, instr);
	}
	else
		uc->uc_mcontext.arm_pc += 4;
}

// vsnprintf hook (broken on android 5+)
#if 0
//#define STB_SPRINTF_DECORATE(name) name
#define STB_SPRINTF_IMPLEMENTATION
#include "stb_sprintf.h"

int vsnprintf_log(char *str, size_t size, const char *format, va_list ap)
{
	int i = stbsp_vsnprintf(str, size, format, ap);
	static volatile int semaphore;

	if( !semaphore )
	{
		semaphore = 1;
		__android_log_print(ANDROID_LOG_INFO, TAG, "HL2WRAPPER: %s\n", str);
		semaphore = 0;
	}
	return i;
}

#define HIJACK_SIZE 12
#include <sys/mman.h>
#include <unistd.h>
void simple_hook( void *target, void *func )
{
	unsigned char o_code[HIJACK_SIZE], n_code[HIJACK_SIZE];
	size_t pagesize = sysconf(_SC_PAGESIZE);

	if ( (unsigned long)target % 4 == 0 )
	{
		// ldr pc, [pc, #0]; .long addr; .long addr
		memcpy(n_code, "\x00\xf0\x9f\xe5\x00\x00\x00\x00\x00\x00\x00\x00", HIJACK_SIZE);
		*(unsigned long *)&n_code[4] = (unsigned long)func;
		*(unsigned long *)&n_code[8] = (unsigned long)func;
	}
	else // Thumb
	{
		// add r0, pc, #4; ldr r0, [r0, #0]; mov pc, r0; mov pc, r0; .long addr
		memcpy(n_code, "\x01\xa0\x00\x68\x87\x46\x87\x46\x00\x00\x00\x00", HIJACK_SIZE);
		*(unsigned long *)&n_code[8] = (unsigned long)func;
		target--;
	}

	mprotect((char *)(((int) target - (pagesize-1)) & ~(pagesize-1)), pagesize * 2, PROT_READ | PROT_WRITE );

	memcpy(o_code, target, HIJACK_SIZE);

	memcpy(target, n_code, HIJACK_SIZE);
	mprotect((char *)(((int) target - (pagesize-1)) & ~(pagesize-1)), pagesize * 2, PROT_READ | PROT_EXEC );
	cacheflush(target, target + HIJACK_SIZE, 0);
}

#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* x)
{
	// Register SIGILL handler
	struct sigaction sa_sigill, osa, sa_sigtrap;
	__android_log_print(ANDROID_LOG_INFO, TAG, "JNI_OnLoad\n");

	sa_sigill.sa_flags = SA_ONSTACK | SA_RESTART | SA_SIGINFO;
	sa_sigill.sa_sigaction = sighandler;
	sigaction(SIGILL, &sa_sigill, &osa);
	sa_sigtrap.sa_flags = SA_ONSTACK | SA_RESTART | SA_SIGINFO;
	sa_sigtrap.sa_sigaction = sigtrap_handler;
	sigaction(SIGTRAP, &sa_sigtrap, &osa);
	//simple_hook( vsnprintf, vsnprintf_log );

	return JNI_VERSION_1_4;
}

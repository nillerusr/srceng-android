#include <jni.h>
#include <android/log.h>
#define HIJACK_SIZE 12
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdint.h>
#include <ctype.h>
#include <stdlib.h>

#define EXPORT __attribute__((visibility("default")))
#define MAX(a,b) ((a) > (b) ? (a) : (b))
#define ARRAYSIZE(p) (sizeof(p)/sizeof(p[0]))

// Processor Information:
struct CPUInformation
{
    int  m_Size;        // Size of this structure, for forward compatability.

    bool m_bRDTSC : 1,  // Is RDTSC supported?
         m_bCMOV  : 1,  // Is CMOV supported?
         m_bFCMOV : 1,  // Is FCMOV supported?
         m_bSSE   : 1,  // Is SSE supported?
         m_bSSE2  : 1,  // Is SSE2 Supported?
         m_b3DNow : 1,  // Is 3DNow! Supported?
         m_bMMX   : 1,  // Is MMX supported?
         m_bHT    : 1;  // Is HyperThreading supported?

    uint8_t m_nLogicalProcessors;     // Number op logical processors.
    uint8_t m_nPhysicalProcessors;    // Number of physical processors
    
    bool m_bSSE3 : 1,
         m_bSSSE3 : 1,
         m_bSSE4a : 1,
         m_bSSE41 : 1,
         m_bSSE42 : 1;  

    int64_t m_Speed;                      // In cycles per second.

    char* m_szProcessorID;             // Processor vendor Identification.

    uint32_t m_nModel;
    uint32_t m_nFeatures[3];

    CPUInformation(): m_Size(0){}
};


uint64_t _GetCPUFreqFromPROC()
{
    double mhz = 0;
    char line[1024], *s, search_str[] = "cpu MHz";

    /* open proc/cpuinfo */
    FILE *fp = fopen( "/proc/cpuinfo", "r" );
    if (fp == NULL)
    {
        return 0;
    }

    /* ignore all lines until we reach MHz information */
    while (fgets(line, 1024, fp) != NULL) 
    { 
        if (strstr(line, search_str) != NULL) 
        {
            /* ignore all characters in line up to : */
            for (s = line; *s && (*s != ':'); ++s)
                ;

            /* get MHz number */
            if ( *s && ( sscanf( s + 1, "%lf", &mhz) == 1 ) )
                break;
        }
    }

    fclose(fp);

    return ( uint64_t )( mhz * 1000000 );
}

uint64_t _CalculateCPUFreq()
{
    // Try to open cpuinfo_max_freq. If the kernel was built with cpu scaling support disabled, this will fail.
    FILE *fp = fopen( "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r" );
    if ( fp )
    {
        char buf[ 256 ];
        uint64_t retVal = 0;

        buf[ 0 ] = 0;
        if( fread( buf, 1, ARRAYSIZE( buf ), fp ) )
        {
            retVal = ( uint64_t )atoll( buf );
        }
        fclose(fp);

        if( retVal )
        {
            return retVal * 1000;
        }
    }

    return _GetCPUFreqFromPROC();
}


const CPUInformation* _GetCPUInformation()
{
    static CPUInformation pi;

    __android_log_print( ANDROID_LOG_INFO, "TIERHOOK", "_GetCPUInformation()");

    // Has the structure already been initialized and filled out?
    if ( pi.m_Size == sizeof(pi) )
        return &pi;

    // Redundant, but just in case the user somehow messes with the size.
    memset(&pi, 0x0, sizeof(pi));

    // Fill out the structure, and return it:
    pi.m_Size = sizeof(pi);

    // Grab the processor frequency:
    pi.m_Speed = _CalculateCPUFreq();

    // Get the logical and physical processor counts:
    pi.m_nLogicalProcessors = 1;

    // TODO: poll /dev/cpuinfo when we have some benefits from multithreading
    FILE *fpCpuInfo = fopen( "/proc/cpuinfo", "r" );
    if ( fpCpuInfo )
    {
        int nLogicalProcs = 0;
        int nProcId = -1, nCoreId = -1;
        const int kMaxPhysicalCores = 128;
        int anKnownIds[kMaxPhysicalCores];
        int nKnownIdCount = 0;
        char buf[255];
        while ( fgets( buf, ARRAYSIZE(buf), fpCpuInfo ) )
        {
            if ( char *value = strchr( buf, ':' ) )
            {
                for ( char *p = value - 1; p > buf && isspace((unsigned char)*p); --p )
                {
                    *p = 0;
                }
                for ( char *p = buf; p < value && *p; ++p )
                {
                    *p = tolower((unsigned char)*p);
                }
                if ( !strcmp( buf, "processor" ) )
                {
                    ++nLogicalProcs;
                    nProcId = nCoreId = -1;
                }
                else if ( !strcmp( buf, "physical id" ) )
                {
                    nProcId = atoi( value+1 );
                }
                else if ( !strcmp( buf, "core id" ) )
                {
                    nCoreId = atoi( value+1 );
                }

                if (nProcId != -1 && nCoreId != -1) // as soon as we have a complete id, process it
                {
                    int i = 0, nId = (nProcId << 16) + nCoreId;
                    while ( i < nKnownIdCount && anKnownIds[i] != nId ) { ++i; }
                    if ( i == nKnownIdCount && nKnownIdCount < kMaxPhysicalCores )
                        anKnownIds[nKnownIdCount++] = nId;
                    nProcId = nCoreId = -1;
                }
            }
        }
        fclose( fpCpuInfo );
        pi.m_nLogicalProcessors = MAX( 1, nLogicalProcs );
        pi.m_nPhysicalProcessors = MAX( 1, nKnownIdCount );
    }
    else
    {
        pi.m_nPhysicalProcessors = 1;
        pi.m_nLogicalProcessors  = 1;
        __android_log_print( ANDROID_LOG_ERROR, "TIERHOOK", "couldn't read cpu information from /proc/cpuinfo" );
    }
    // Determine Processor Features:
    pi.m_bRDTSC        = false;
    pi.m_bCMOV         = false;
    pi.m_bFCMOV        = false;
    pi.m_bMMX          = false;
    pi.m_bSSE          = false;
    pi.m_bSSE2         = false;
    pi.m_bSSE3         = false;
    pi.m_bSSSE3        = false;
    pi.m_bSSE4a        = false;
    pi.m_bSSE41        = false;
    pi.m_bSSE42        = false;
    pi.m_b3DNow        = false;
    pi.m_szProcessorID = strdup("Generic_x86");
    pi.m_bHT           = false;

    return &pi;
}


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
	cacheflush((long)target, (long)target + HIJACK_SIZE, 0);
}

extern "C" const CPUInformation* GetCPUInformation();

extern "C"
{
    // _SpewMessage hook hook
    int DefaultSpewFunc( int type, char *msg );
    void SpewOutputFunc(int (*func)( int, char*));
    void *GetSpewOutputFunc();


    int _NewSpewMessage( int type, const char *fmt, va_list ap )
    {
    	char log[1000];
    	int (*func)( int, char*);

    	vsnprintf(log, 999, fmt, ap);
    	log[999] = 0;

    	__android_log_print( ANDROID_LOG_INFO, "SRCENGINE", "%s", log );

    	func = GetSpewOutputFunc();

    	if( func && func != DefaultSpewFunc )
    		func( 0, log );

    	return 1;
    }
}

extern "C"
{
	EXPORT void HookInit( )
	{
		// offset seems to be correct in 23-78

		simple_hook( DefaultSpewFunc + 0x50, _NewSpewMessage );
		simple_hook( GetCPUInformation, _GetCPUInformation );
		// force enable text input (no need in SDL patching now)
	}
}

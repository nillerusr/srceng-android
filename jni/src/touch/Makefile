DLLNAME=libtouch
#make sure this is the correct compiler for your system
CXX=gcc

DLL_SRCDIR=.
DLL_OBJDIR=./obj

DEFINES=-DANDROID -D_LINUX -DLINUX -DPOSIX -D_POSIX -DGNUC -D__ANDROID__
SRCS = $(wildcard *.c)
OBJS = $(patsubst %.c,obj/%.o,$(SRCS))
#OBJS += libclient.so
CFLAGS=-O3 -mfloat-abi=hard -mfpu=neon-vfpv4 -mcpu=cortex-a15 -mhard-float -g -std=c99
INCLUDES=-I.
LDFLAGS=-shared -llog -Wl,--no-undefined,--no-warn-mismatch

obj/%.o : %.c
	$(CXX) $(CFLAGS) $(INCLUDES) $(DEFINES) -c "$<" -o "$@"

$(DLLNAME).so : neat $(OBJS)
	$(CXX) -o $(DLLNAME).so $(LDFLAGS) $(OBJS)
neat:
	if ! [ -d $(DLL_OBJDIR) ];then mkdir $(DLL_OBJDIR);fi
clean:
	-rm -fr $(DLL_OBJDIR)
	-rm -f $(DLLNAME).so

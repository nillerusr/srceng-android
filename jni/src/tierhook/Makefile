DLLNAME=libtierhook

ifeq ($(NDK),1)
	CC = $(shell $(NDK_PATH)/ndk-which gcc)
	CXX = $(shell $(NDK_PATH)/ndk-which g++)
	LD = $(shell $(NDK_PATH)/ndk-which ld)
	AR = $(shell $(NDK_PATH)/ndk-which ar)
	LINK = $(shell $(NDK_PATH)/ndk-which g++)
	STRIP = $(shell $(NDK_PATH)/ndk-which strip)
else
	CC=gcc
endif

DLL_SRCDIR=.

SRCS_C = tierhook.c
OBJS += $(patsubst %.c,%.o,$(SRCS_C))

OPTFLAGS=-O2 -fno-rtti -fno-omit-frame-pointer -march=armv7-a -mtune=cortex-a15 -mthumb -mfloat-abi=softfp -mfpu=neon -mcpu=cortex-a9 -pipe -mvectorize-with-neon-quad
CXXFLAGS=
CFLAGS=

INCLUDES=-I.
LDFLAGS = -L../../../libs/armeabi-v7a -Wl,--no-undefined -shared -llog -ltier0 -landroidwrapper

ifeq ($(NDK),1)
	SYSROOT := "$(NDK_PATH)/platforms/android-$(APP_API_LEVEL)/arch-arm"
	CFLAGS += "--sysroot=$(SYSROOT)"
	CXXFLAGS += "--sysroot=$(SYSROOT)"
	LDFLAGS += -L$(SYSROOT)/usr/lib -Wl,--build-id --sysroot=$(SYSROOT)
endif

%.o : %.c
	$(CC) $(OPTFLAGS) $(CFLAGS) $(INCLUDES) $(DEFINES) -c "$<" -o $(PWD)/"$@"

$(DLLNAME).so : $(OBJS)
	$(CC) -o $(DLLNAME).so $(OPTFLAGS) $(LDFLAGS) $(OBJS) $(LIBS)
clean:
	-rm -f $(OBJS)
	-rm -f $(DLLNAME).so

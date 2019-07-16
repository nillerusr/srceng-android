CC=gcc
CFLAGS= -O3
FLAGS= -Wl,--no-undefined -lstdc++ -llog
LDFLAGS= -shared

SOURCES := tierhook.c
OBJECTS := tierhook.o libtier0.so

TARGET := libtierhook.so

all: $(TARGET)

clean:
	rm -f tierhook.o

$(TARGET) : $(OBJECTS)
	$(CC) $(FLAGS) $(CFLAGS) $(OBJECTS) -o $@ $(LDFLAGS)

# Tang nano 9k Chisel sample

## How to build

```sh
mkdir build
cd build
cmake ..
make chisel
make uart_tx
```

## Write Bitstream with openFPGALoader

```sh
cd build
make uart_tx.openFPGALoader
```

## CMake Option

### Set GWIN Path

```sh
cmake -DGWIN_ROOT=<GWIN install directory> ..
```

Note: `GWIN_ROOT` is the parent path of `IDE/bin/gw_ide`.

```
$ ls $GWIN_ROOT
IDE Programmer
```

### Set openFPGALoader Path

```sh
cmake -DOPENFPGALOADER_ROOT=<openFPGALoader install directory> ..
```

#include <iostream>
#include "plc_device.h"

PPlcDevice Device;

int main() {
    Device = new TPlcDevice();
    Device->AskForUserId();
    Device->Start();
    std::cout<<"PLC:Started@0"<<std::endl;
    while(std::cin.get() != 'q');
    Device->Stop();
    std::cout<<"PLC:Bye@0"<<std::endl;
    delete Device;
    return 0;
}

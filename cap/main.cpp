#include <iostream>
#include <sstream>
#include "pcap.h"

pcap_if_t* headOfDevices = NULL;
pcap_if_t* selectedDevice = NULL;
pcap_t* adHandle = NULL;

typedef struct {
    u_char  versionAndHeadLength;           // Version (4 bits) + Internet header length (4 bits)
    u_char  typeOfService;                  // Type of service
    u_short totalLength;                    // Total length
    u_short identification;                 // Identification
    u_short flagsAndFragmentOffset;         // Flags (3 bits) + Fragment offset (13 bits)
    u_char  timeToLive;                     // Time to live
    u_char  protocol;                       // Protocol
    u_short crc;                            // Header checksum
    u_int   srcAddr;                        // Source address
    u_int   dstAddr;                        // Destination address
} ip_header;

typedef struct {
    u_short srcPort;
    u_short dstPort;
    u_int   sequenceNumber;
    u_int   acknowledgeNumber;
    u_char  dataOffset;
    u_char  flags;
    u_short windowSize;
} tcp_header;

u_int swapDoubleWord(u_int x) {
    return 0xFF000000 & (x << 24) | 0x00FF0000 & (x << 8) | 0x0000FF00 & (x >> 8) | 0x000000FF & (x >> 24);
}

u_short swapWord(u_short x) {
    return 0xFF00 & (x << 8) | 0x00FF & (x >> 8);
}

void selectDevice() {
    char buf[255];
    pcap_if_t *currentDevice;
    int index, numberOfDevices;
    std::cout<<"PCAP:Please select network device(interface): @0"<<std::endl;
    if (pcap_findalldevs_ex(PCAP_SRC_IF_STRING, NULL, &headOfDevices, buf) == -1) {
        std::cout<<"Failed to iterate network devices(interfaces): %s@0"<<std::endl;
        exit(1);
    }
    index = 0;
    for (currentDevice = headOfDevices; currentDevice; currentDevice = currentDevice->next) {
        std::cout<<++index<<"> "<<currentDevice->name;
        if (currentDevice->description) {
            std::cout<<" ("<<currentDevice->description<<")@0"<<std::endl;
        } else {
            std::cout<<" (No description available)@0"<<std::endl;
        }
    }
    numberOfDevices = index;
    if (numberOfDevices == 0) {
        std::cout<<"No interfaces found! Make sure WinPcap is installed.@0"<<std::endl;
        exit(1);
    } else {
        index = 0;
        while (index <= 0 || index > numberOfDevices) {
            std::cout<<"Enter the interface number (1-"<<numberOfDevices<<"):@0"<<std::endl;
            std::cin>>index;
        }
    }
    for (currentDevice = headOfDevices; currentDevice; currentDevice = currentDevice->next) {
        index--;
        if (index <= 0) {
            break;
        }
    }
    selectedDevice = currentDevice;
}

void startCapture() {
    char buf[255];
    std::stringstream exp;
    pcap_addr_t * currentAddress;
    int netmask = 0x00FFFFFF;
    int addr;
    struct bpf_program fcode;
    if ((adHandle = pcap_open(selectedDevice->name, 65536, PCAP_OPENFLAG_PROMISCUOUS, 1000, NULL, buf) ) == NULL) {
        std::cout<<"Unable to open the adapter. "<<selectedDevice->name<<" is not supported by WinPcap@0"<<std::endl;
        std::cout<<"The error is : "<<buf<<"@0"<<std::endl;
        pcap_freealldevs(headOfDevices);
        return;
    }
    exp.str("");
    exp<<"tcp";
    for (currentAddress = selectedDevice->addresses; currentAddress; currentAddress = currentAddress->next) {
        if (currentAddress->addr->sa_family == AF_INET) {
            netmask = ((struct sockaddr_in *)(currentAddress->netmask))->sin_addr.s_addr;
            addr = ((struct sockaddr_in *)(currentAddress->addr))->sin_addr.s_addr;
            exp<<" and not src host "<<(0xFF & addr)<<"."<<(0xFF & (addr >> 8))<<"."<<(0xFF & (addr >> 16))<<"."<<(0xFF & (addr >> 24));
            break;
        }
    }
    std::cout<<"PCAP:FilterExpression=\'"<<exp.str()<<"\'@0"<<std::endl;
    if (pcap_compile(adHandle, &fcode, exp.str().c_str(), 1, netmask) < 0) {
        std::cout<<"Unable to compile the packet filter. Check the syntax.@0"<<std::endl;
        pcap_freealldevs(headOfDevices);
        return;
    }
    pcap_freealldevs(headOfDevices);
    headOfDevices = NULL;
    selectedDevice = NULL;
    if (pcap_setfilter(adHandle, &fcode) < 0) {
        std::cout<<"Error setting the filter.@0"<<std::endl;
    } else {
        std::cout<<"Capture started successfully.@0"<<std::endl;
    }
}

unsigned long __stdcall blockUtilQuit(void* lp) {
    while(std::cin.get() != 'q');
    adHandle = NULL;
    Sleep(3000);
    exit(0);
}

void printFlags(u_char f) {
    if (f & 0x20) {
        std::cout << "URG ";
    }
    if (f & 0x10) {
        std::cout << "ACK ";
    }
    if (f & 0x08) {
        std::cout << "PSH ";
    }
    if (f & 0x04) {
        std::cout << "RST ";
    }
    if (f & 0x02) {
        std::cout << "SYN ";
    }
    if (f & 0x01) {
        std::cout << "FIN ";
    }
}

void workLoop() {
    int result;
    pcap_t* lcHandle;
    struct pcap_pkthdr* header;
    const u_char* pkt_data;
    ip_header* ih;
    tcp_header* th;
    while (true) {
        lcHandle = adHandle;
        if (lcHandle == NULL) {
            break;
        }
        result = pcap_next_ex(lcHandle, &header, &pkt_data);
        if (result < 0) {
            printf("PCAP:Error reading packets, capture aborted.@0\r\n");
            break;
        } else if (result == 0) {
            continue;
        }
        ih = (ip_header*) (pkt_data + 14);
        th = (tcp_header*) ((u_char*) ih + (ih->versionAndHeadLength & 0xf) * 4);
        std::cout << "{";
        std::cout << (u_int)(header->ts.tv_sec) << ",";
        std::cout << (u_int)(header->ts.tv_usec) << ",";
        std::cout << header->len << ",";
        std::cout << swapDoubleWord(ih->srcAddr) << ",";
        std::cout << swapDoubleWord(ih->dstAddr) << ",";
        std::cout << swapWord(th->srcPort) << ",";
        std::cout << swapWord(th->dstPort) << ",";
        //printFlags(th->flags);
        std::cout << (u_int)(th->flags);
        std::cout << "}@IDS" << std::endl;
    }
}

int main() {
    std::cout<<"PCAP:Welcome!@0"<<std::endl;
    selectDevice();
    startCapture();
    CreateThread(NULL, 0, blockUtilQuit, NULL, 0, NULL);
    workLoop();
    std::cout<<"PCAP:Bye@0"<<std::endl;
    return 0;
}

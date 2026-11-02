// Reference generator. GPLv3, uses the unchanged Android engine's public host C API.
#include "spektra.h"
#include <fstream>
#include <vector>
#include <string>
#include <cstdio>
int main(int argc,char**argv){
 if(argc!=9)return 2;
 const int w=std::stoi(argv[4]),h=std::stoi(argv[5]);
 std::vector<float> input((size_t)w*h*3);
 std::ifstream f(argv[2],std::ios::binary);f.read((char*)input.data(),input.size()*4);
 if(!f)return 3;
 spk_engine* e=nullptr; auto st=spk_engine_create(argv[1],&e);if(st)return st;
 spk_params p{};p.film_profile=argv[6];p.print_profile=argv[7];spk_default_params(&p);
 p.scan_film=std::stoi(argv[8]);p.output_color_space=SPK_CS_SRGB;p.output_cctf_encoding=1;
 spk_image in{input.data(),w,h,SPK_CS_PROPHOTO},out{};
 st=spk_simulate(e,&in,&p,&out);
 if(st){fprintf(stderr,"render error %d\n",st);return st;}
 std::ofstream o(argv[3],std::ios::binary);o.write((char*)out.data,(size_t)out.width*out.height*3*4);
 printf("%s/%s scan=%d -> %dx%d\n",p.film_profile,p.print_profile,p.scan_film,out.width,out.height);
 spk_image_free(&out);spk_engine_destroy(e);return !o;
}

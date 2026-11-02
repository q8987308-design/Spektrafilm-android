from pathlib import Path
from PIL import Image,ImageCms
import numpy as np,json,subprocess,hashlib,shutil,datetime
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
ASSETS=ROOT/'engine/spektra-core/src/main/assets/spektra'
OUT=HERE/'film_references';OUT.mkdir(exist_ok=True)
hashfile=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
# Same IEC sRGB decoding and float32 matrix as ImagePipeline.kt.
base=Image.open(HERE/'coffee.png').convert('RGB')
base.thumbnail((360,360),Image.Resampling.LANCZOS)
a=np.asarray(base,dtype=np.float32)/np.float32(255)
linear=np.where(a<=np.float32(.04045),a/np.float32(12.92),((a.astype(np.float64)+.055)/1.055)**2.4).astype(np.float32)
m=np.array([[.5290825,.3303437,.1405738],[.0982640,.8734031,.0283329],[.0167029,.1176946,.8656026]],dtype=np.float32)
prophoto=np.stack([(m[c,0]*linear[:,:,0]+m[c,1]*linear[:,:,1])+m[c,2]*linear[:,:,2] for c in range(3)],axis=-1).astype('<f4');prophoto.tofile(HERE/'baseline-prophoto.f32')
base.save(HERE/'baseline.jpg',quality=95)
stocks=json.loads((ASSETS/'catalog.json').read_text(encoding='utf-8'))['stocks']
records=[]
icc=(ASSETS/'icc/saucecontrol/sRGB-v4.icc').read_bytes()
for id,s in stocks.items():
 film=id if s['kind']=='film' else 'kodak_portra_400'
 paper='kodak_portra_endura' if s['kind']=='film' else id
 scan=1 if s['group']=='color_reversal' else 0
 raw=HERE/(id+'.f32')
 cmd=[str(HERE/'render_reference.exe'),str(ASSETS),str(HERE/'baseline-prophoto.f32'),str(raw),str(base.width),str(base.height),film,paper,str(scan)]
 r=subprocess.run(cmd,capture_output=True,text=True)
 print(r.stdout or r.stderr,flush=True)
 if r.returncode:raise RuntimeError((id,r.returncode,r.stderr))
 rgb=np.fromfile(raw,dtype='<f4').reshape(base.height,base.width,3)
 if not np.isfinite(rgb).all():raise ValueError('non-finite output '+id)
 Image.fromarray(np.rint(np.clip(rgb,0,1)*255).astype(np.uint8)).save(OUT/(id+'.jpg'),quality=92,subsampling=0,icc_profile=icc)
 records.append(dict(id=id,kind=s['kind'],film=film,paper=paper,scan_film=scan,width=base.width,height=base.height,sha256=hashfile(OUT/(id+'.jpg')),raw_sha256=hashfile(raw),min=float(rgb.min()),max=float(rgb.max())))
manifest=dict(schema=1,created='2026-11-02',source_commit='d7f9fcf86213c9dc46d2a2b5ddfa41dc8040ef0e',base=dict(file='coffee.png',sha256=hashfile(HERE/'coffee.png'),url='https://raw.githubusercontent.com/scikit-image/scikit-image/v0.25.2/skimage/data/coffee.png',photographer='Rachel Michetti',license='CC0',license_evidence='https://scikit-image.org/docs/stable/api/skimage.data.html#skimage.data.coffee'),pipeline='ImagePipeline.kt sRGB CCTF decode and float32 SRGB_TO_PROPHOTO; spk_default_params; film/paper/scan_film only changes; spk_simulate CPU full pipeline; sRGB encoded JPEG quality92',engine_cpp_sha256=hashfile(ROOT/'engine/spektra-core/src/main/cpp/spektra.cpp'),compiler='w64devkit 2.11.0 GCC 16.2.0',flags='-std=gnu++17 -D_USE_MATH_DEFINES -O3 -ffast-math -fno-finite-math-only -pthread',records=records)
(HERE/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print('COMPLETE',len(records))





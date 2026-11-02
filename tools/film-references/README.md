# 固定胶片参考图生成

基准照片 Coffee cup，Rachel Michetti，CC0。来源和许可证据见 manifest.json，照片也随本目录提供。

在仓库根目录：
1. 使用 GCC（本次为 w64devkit 2.11.0 / GCC16.2.0）运行 `powershell -File tools/film-references/build-host.ps1 -Compiler <g++.exe路径>`。
2. 在包含 numpy/Pillow 的 Python 环境运行 `python tools/film-references/generate.py`。
3. 生成器会在本目录生成 `film_references/`、原始 float 缓冲和 manifest.json。核对结果后将 JPEG、manifest 复制到 app/src/main/assets/film_references/。

引擎使用原始 C++ 源码；输入使用应用同样的 sRGB 解码和线性 ProPhoto 转换。负片搭配 Kodak Portra Endura，反转片直接扫描，相纸参考搭配 Portra400；其余参数为引擎默认值。输出附 sRGB ICC。默认条件的参考效果不代表用户自定义参数或任意光源下的结果。

GPLv3；原作者署名与上游许可均保留。生成命令和编译器版本记录在 manifest.json。图片曾重复生成两次，JPEG与原始像素哈希一致。

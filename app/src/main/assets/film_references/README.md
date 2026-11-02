# 固定参考图说明

基准照片：Coffee cup，摄影 Rachel Michetti，Pikolo Espresso Bar；CC0。
来源：https://raw.githubusercontent.com/scikit-image/scikit-image/v0.25.2/skimage/data/coffee.png
许可证据：https://scikit-image.org/docs/stable/api/skimage.data.html#skimage.data.coffee

所有卡片使用同一张照片（等比例缩至360×240），不读取用户照片。
生成链路：应用 ImagePipeline.kt 的 sRGB 解码和 SRGB_TO_PROPHOTO 矩阵 → 未修改的 C++ 原生引擎 spk_default_params + spk_simulate → sRGB 编码 JPEG。
彩色负片和电影负片：所选胶片 + Kodak Portra Endura 相纸；反转片：所选胶片直接扫描；相纸卡片：Kodak Portra 400 + 所选相纸。
曝光采用默认 center_weighted 自动曝光；胶片/相纸配置、颗粒、光晕、耦合及其它参数保持引擎默认值。
因此参考图表示统一默认暗房条件，不承诺与用户当前自定义参数或其它光源下的照片一致。

复现：Windows PowerShell 运行 build-host.ps1，再用包含 numpy/Pillow 的 Python 运行 generate.py。宿主编译器放在 ../tools/w64devkit，Android 产品源码和引擎算法未作修改。
manifest.json 记录基准图、输出 JPEG、输出 float32 数据和引擎文件哈希。JPEG 附带标准 sRGB ICC。

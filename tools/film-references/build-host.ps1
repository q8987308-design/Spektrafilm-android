param([string]$Compiler = 'g++')
$ErrorActionPreference='Stop'
$base=$PSScriptRoot
$cpp=Join-Path $base '../../engine/spektra-core/src/main/cpp'
$sources=@('spektra.cpp','gpu/*.cpp','ml/*.cpp','kernels/*.cpp','io/*.cpp','model/*.cpp','profiles/*.cpp','runtime/*.cpp','runtime/stages/*.cpp') | ForEach-Object { Get-ChildItem (Join-Path $cpp $_) } | ForEach-Object FullName
& $Compiler -std=gnu++17 -D_USE_MATH_DEFINES -O3 -ffast-math -fno-finite-math-only -pthread "-I$cpp" "$base/render_reference.cpp" @sources -o "$base/render_reference.exe"
if ($LASTEXITCODE -ne 0) { throw 'Reference renderer build failed' }

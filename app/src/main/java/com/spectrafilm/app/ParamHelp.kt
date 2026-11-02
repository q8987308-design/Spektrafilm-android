/*
 * Spektrafilm for Android — plain-language help for the opaque controls. GPLv3.
 * Film modeling powered by spektrafilm.
 *
 * The editor exposes the engine's real, physically-named knobs (DIR couplers, halation
 * scatter, density-curve gamma…). They are honest but intimidating. This is the onboarding
 * answer (USER_DRIVEN_SOLUTIONS.md §6h, pain #8): a central registry of friendly,
 * photographer-facing explanations, surfaced by the "?" help sheet on each section header.
 *
 * Relabel-only — the engine still receives the identical params, so this NEVER changes the
 * render and carries zero parity risk. Pure data (no Android/Compose types) so the content
 * coverage is JVM-unit-testable (ParamHelpTest).
 */
package com.spectrafilm.app

/**
 * One control's help: a [title], a one-line [summary] (the at-a-glance "what it does") and a
 * fuller [body] (what it does to the photo + when to reach for it), written for a photographer
 * rather than a film chemist.
 */
data class ParamHelp(
    val title: String,
    val summary: String,
    val body: String,
)

/** Central registry of plain-language help, keyed by a stable id (not the display label). */
object ParamHelpText {
    // Stable keys so relabeling a section never breaks the lookup (or its test).
    const val GRAIN = "grain"
    const val HALATION = "halation"
    const val COUPLERS = "couplers"
    const val PRINT_GAMMA = "print_gamma"
    const val PREFLASH = "preflash"
    const val GLARE = "glare"

    val sections: Map<String, ParamHelp> = mapOf(
        GRAIN to ParamHelp(
            title = "颗粒",
            summary = "让胶片呈现独特质感的细密颗粒纹理。",
            body = "真实胶片由微小的感光颗粒组成，颗粒随机聚集，便形成了可见的颗粒感。颗粒越大（感光度 ISO 越高），纹理就越粗、越明显；各通道的缩放可让某一色层比其他色层更有颗粒感。降低可获得干净现代的效果，提高则更粗粝复古。颗粒以微米为单位，因此在屏幕小预览中可能比全分辨率导出时更细。",
        ),
        HALATION to ParamHelp(
            title = "光晕",
            summary = "明亮高光周围柔和泛红的辉光。",
            body = "在真实胶片中，强光穿过乳剂，经片基背面反射后，会在路灯、透过树叶的阳光、镜面高光等明亮区域周围再次曝光。“光晕量”控制辉光强度，“散射”模拟乳剂内范围更广、更柔和的光扩散；“提升 EV”决定高光达到多亮时才产生辉光。提高可获得梦幻的电影感，关闭整个分区则让高光干净利落。",
        ),
        COUPLERS to ParamHelp(
            title = "胶片色彩特性（成色剂）",
            summary = "胶片独有的色彩分离、边缘力度与饱和度过渡。",
            body = "胶片显影时，一个色层中形成的染料会通过化学作用抑制相邻色层，这就是“DIR 成色剂”效应。这种层间影响造就了胶片独特的色彩：更鲜明的边缘，以及独特的非线性饱和度。“效果强度”调整整体特性，跨色控制则决定各颜色之间的相互影响程度。这是色彩风格控制；如需常规增减色彩浓度，请使用“输出”中的“饱和度”和“自然饱和度”。",
        ),
        PRINT_GAMMA to ParamHelp(
            title = "胶片与印相对比度（伽马）",
            summary = "负片和相纸本身的基础对比度。",
            body = "每种胶片和相纸都有特性曲线，“伽马”表示曲线的陡峭程度，相当于光化学过程中的对比度控制。提高印相伽马会让照片更有力度、对比更强；降低则更平缓。日常微调请优先使用“对比度”滑块；这些基础光化学参数用于塑造影调骨架。",
        ),
        PREFLASH to ParamHelp(
            title = "预闪光",
            summary = "用微弱均匀的光预先曝光相纸，柔化强烈对比。",
            body = "暗房印相有时会在正式曝光前，用微弱均匀的光预闪相纸。这能提亮最深的阴影，温和降低印相对比度，同时保留高光。提高“曝光”可让阴影更柔和、细节更通透；Y 和 M 滤色偏移可改变预闪光的色调，原理与放大机滤色相同。",
        ),
        GLARE to ParamHelp(
            title = "眩光",
            summary = "光线在印相表面散射形成的薄雾感。",
            body = "此项模拟印相和扫描器内部的杂散反射光，略微抬升黑位，为画面蒙上柔和薄雾。它具有随机性，因此默认关闭，也不包含在要求精确、可重复的导出中。提高“百分比”可增强雾感、降低对比，营造更怀旧的氛围。",
        ),
    )

    /** The help for [key], or null if none is registered. */
    fun forKey(key: String): ParamHelp? = sections[key]
}

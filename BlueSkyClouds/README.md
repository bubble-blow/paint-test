# BlueSkyClouds (Eclipse Android / ADT)

一个可直接导入 Eclipse + ADT 的 Android 示例项目，使用 `Canvas` 纯代码绘制蓝天白云，不依赖任何图片素材。

## 导入方式
1. 打开 Eclipse（已安装 ADT 插件）。
2. `File` -> `Import` -> `Android` -> `Existing Android Code Into Workspace`。
3. 选择本目录下的 `BlueSkyClouds`。
4. 点击 `Finish`。

## 说明
- 入口 Activity：`MainActivity`
- 自定义绘制 View：`SkyCloudView`
- 天空：三段式线性渐变（深蓝 -> 天蓝 -> 近地平线浅蓝）。
- 云朵：多层云团叠加（阴影层 + 主体层 + 柔化层 + 高光层），并结合 `BlurMaskFilter` 做边缘羽化，提升体积感。
- 云朵远近：通过大小、透明度、位置分层，近景更大更清晰，远景更轻更淡。

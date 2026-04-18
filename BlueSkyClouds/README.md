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
- 使用线性渐变绘制天空，并通过多个椭圆叠加绘制云朵。

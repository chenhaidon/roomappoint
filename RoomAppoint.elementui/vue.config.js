const { defineConfig } = require("@vue/cli-service");
const webpack = require("webpack"); //导入 webpack 模块

module.exports = {
  transpileDependencies: true,
  lintOnSave: false,
  //在模块中加入
  configureWebpack: {
    plugins: [
      new webpack.ProvidePlugin({
        "window.Quill": "quill/dist/quill.js",
        Quill: "quill/dist/quill.js",
      }),
    ],
  },
  //配置开发服务器
  devServer: {
     allowedHosts: 'all', // 或指定你的域名: ['6314iq108bs1.vicp.fun']
    client: {
      // 取消编译错误 全屏覆盖
      overlay: false,
    },
  },
  css: {
    loaderOptions: {
      sass: {
        // 这里的选项会传递给 sass-loader
      },
    },
  },
};

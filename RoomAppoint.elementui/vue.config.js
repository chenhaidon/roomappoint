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
    host: '0.0.0.0',
    port: 8081,
    allowedHosts: 'all',
    client: {
      overlay: false,
    },
    proxy: {
      '/api': {
        target: 'http://localhost:7245',
        changeOrigin: true,
        pathRewrite: { '^/api': '' },
      },
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

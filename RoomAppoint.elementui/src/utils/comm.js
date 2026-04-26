import { Message, MessageBox, Loading } from "element-ui"; //消息提示框
import store from "@/store";
import router from "@/router";

const BASE_API = (process.env.VUE_APP_BASE_API || "").replace(/\/+$/, "");

function resolveWindowFn(name) {
  if (typeof window !== "undefined" && typeof window[name] === "function") {
    return window[name];
  }
  return null;
}

function pickFirstValue(value) {
  if (value === undefined || value === null) {
    return "";
  }
  let raw = Array.isArray(value) ? value[0] : value;
  raw = String(raw).trim();
  if (!raw) {
    return "";
  }
  if (raw.startsWith("[") && raw.endsWith("]")) {
    try {
      const arr = JSON.parse(raw);
      raw = Array.isArray(arr) ? arr[0] || "" : raw;
    } catch (e) {}
  }
  return String(raw).split(",")[0].trim().replace(/^['\"]|['\"]$/g, "");
}

function toDate(value) {
  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function pad2(value) {
  return String(value).padStart(2, "0");
}

/**
 * 公共确认提示框
 * @param {*} config
 * @returns
 */
export function ConfirmMessageBox(config) {
  let _config = {
    title: config.title || "提示",
    content: config.content || "",
  };
  return new Promise(function (resolve, reject) {
    MessageBox.confirm(_config.content, _config.title, {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => {
        resolve(true);
      })
      .catch(() => {
        resolve(false);
      });
  });
}

/**
 * 获取路径中的文件名称
 */
export function GetFileNameByPath(path) {
  const fn = resolveWindowFn("GetFileNameByPath");
  if (fn) {
    return fn(path);
  }
  const raw = pickFirstValue(path);
  if (!raw) {
    return "";
  }
  const pure = raw.split("?")[0];
  const arr = pure.split("/");
  return arr[arr.length - 1] || "";
}
/**
 * 获取路径中的文件格式
 */
export function GetFileTypeByPath(path) {
  const fn = resolveWindowFn("GetFileTypeByPath");
  if (fn) {
    return fn(path);
  }
  const fileName = GetFileNameByPath(path);
  const index = fileName.lastIndexOf(".");
  return index >= 0 ? fileName.slice(index + 1).toLowerCase() : "";
}
/**
 * 切割字段返回一个集合
 * @param {*} value 需要切割字段
 * @param {*} cutting 切割的符号
 */
export function ConvertArray(value = "", cutting = ",") {
  const fn = resolveWindowFn("ConvertArray");
  if (fn) {
    return fn(value, cutting);
  }
  if (value === undefined || value === null) {
    return [];
  }

  if (Array.isArray(value)) {
    return value.map((x) => String(x).trim()).filter(Boolean);
  }

  let raw = String(value).trim();
  if (!raw) {
    return [];
  }

  if (raw.startsWith("[") && raw.endsWith("]")) {
    try {
      const arr = JSON.parse(raw);
      if (Array.isArray(arr)) {
        return arr.map((x) => String(x).trim()).filter(Boolean);
      }
    } catch (e) {}
  }

  return raw
    .split(cutting)
    .map((x) => String(x).trim().replace(/^['\"]|['\"]$/g, ""))
    .filter(Boolean);
}
/**
 * 根据路径获取文件的详细信息
 */
export function FullConvertUrlArray(value = "", cutting = ",") {
  var arr = ConvertArray(value, cutting);

  arr = arr.map((x) => {
    return {
      url: ReplaceImageHttp(x),
      name: GetFileNameByPath(x),
      type: GetFileTypeByPath(x),
    };
  });
  return arr;
}
/**
 * 处理图片路径
 * @param {*} value
 * @returns
 */
export function ReplaceImageHttp(value) {
  const fn = resolveWindowFn("ReplaceImageHttp");
  if (fn) {
    return fn(value);
  }
  const raw = pickFirstValue(value);
  if (!raw) {
    return "";
  }
  if (/^https?:\/\//i.test(raw)) {
    return raw;
  }
  const normalized = raw.startsWith("/") ? raw : `/${raw}`;
  return BASE_API ? `${BASE_API}${normalized}` : normalized;
}

/**
 * 得到完整的时间格式 年-月-日 时分秒
 */
export function YMDHMSFormat(val) {
  const fn = resolveWindowFn("YMDHMSFormat");
  if (fn) {
    return fn(val);
  }
  const date = toDate(val);
  if (!date) {
    return "";
  }
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())} ${pad2(date.getHours())}:${pad2(date.getMinutes())}:${pad2(date.getSeconds())}`;
}
/**
 * 得到完整的时间格式 年-月-日
 */
export function YMDFormat(val) {
  const fn = resolveWindowFn("YMDFormat");
  if (fn) {
    return fn(val);
  }
  const date = toDate(val);
  if (!date) {
    return "";
  }
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
}

/**
 * 得到时分秒格式
 */
export function HMSFormat(val) {
  const fn = resolveWindowFn("HMSFormat");
  if (fn) {
    return fn(val);
  }
  const date = toDate(val);
  if (!date) {
    return "";
  }
  return `${pad2(date.getHours())}:${pad2(date.getMinutes())}:${pad2(date.getSeconds())}`;
}
/**
 * 获取相对时间描述
 * @param {Date|string|number} date 要比较的时间
 * @returns {string} 返回相对时间描述
 */
export function GetRelativeTimeDesc(date) {
  const fn = resolveWindowFn("GetRelativeTimeDesc");
  if (fn) {
    return fn(date);
  }
  const target = toDate(date);
  if (!target) {
    return "";
  }
  const diff = Date.now() - target.getTime();
  const absDiff = Math.abs(diff);
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (absDiff < minute) {
    return "刚刚";
  }
  if (absDiff < hour) {
    const n = Math.floor(absDiff / minute);
    return diff >= 0 ? `${n}分钟前` : `${n}分钟后`;
  }
  if (absDiff < day) {
    const n = Math.floor(absDiff / hour);
    return diff >= 0 ? `${n}小时前` : `${n}小时后`;
  }
  const n = Math.floor(absDiff / day);
  return diff >= 0 ? `${n}天前` : `${n}天后`;
}

/**
 * 检查是否登录状态
 * @returns
 */
export function CheckIsLogin() {
  if (!store.getters.Token) {
    setTimeout(() => {
      router.push("/login");
    }, 500);
    Message({
      showClose: true,
      message: "请先登录后,再操作",
      type: "error",
    });
    return false;
  }
  return true;
}

export default {
  CheckIsLogin,
  ConfirmMessageBox,
  ConvertArray,
  FullConvertUrlArray,
  GetFileNameByPath,
  GetFileTypeByPath,
  ReplaceImageHttp,
  YMDHMSFormat,
  YMDFormat,
  HMSFormat,
  GetRelativeTimeDesc,
};

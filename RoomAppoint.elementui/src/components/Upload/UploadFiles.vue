<template>
    <div class="upload-files-wrap">
        <el-upload :action="uploadUrl" list-type="text" :show-file-list="true" :on-success="handleUploadSuccess"
            :on-remove="handleRemove" :file-list="fileList" :limit="limit" :multiple="true">
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip upload-tip">请上传资源文件<span class="upload-tip-highlight">最大可上传大小不超过1GB:</span>
            </div>
        </el-upload>
    </div>
</template>

<script>
import { ReplaceImageHttp, GetFileNameByPath } from "@/utils/comm.js";
export default {
    name: "UploadImages",
    props: {
        value: { type: [Number, String], default: '' },
        limit: {
            type: Number,
            default: 1,
        },
    },
    data() {
        return {
            uploadUrl: process.env.VUE_APP_BASE_API + "/File/BatchUpload",
            dialogImageUrl: "",
            fileList: []

        };
    },

    mounted() {
        if (this.$props.value) {
            this.fileList = this.$props.value.split(",").map(x => { return { url: ReplaceImageHttp(x), name: GetFileNameByPath(ReplaceImageHttp(x)), status: "success" }; });
        }
    },

    methods: {
        FileListConvert(files) {
            let list = [];
            if (Array.isArray(files)) {
                files.filter(x => x.status == "success").forEach((item) => {
                    if (item.response != null) {
                        list = [...list, { name: "", url: ReplaceImageHttp(item.response.Data[0].Url) }];
                    }
                    else {
                        list = [...list, item];
                    }
                })
            }
            return list;
        },
        async handleUploadSuccess(...args) {
            const fileList = args[2] || [];
            let fs = this.FileListConvert(fileList);
            let url = fs.length > 0 ? fs.map(x => x.url).join(",") : "";
            this.$emit('input', url);
        },
        async handleRemove(...args) {
            const fileList = args[1] || [];
            let fs = this.FileListConvert(fileList);
            let url = fs.length > 0 ? fs.map(x => x.url).join(",") : "";
            this.$emit('input', url);
        },
    },
};
</script>

<style scoped>
.upload-files-wrap {
    border: 1px dashed var(--lib-border);
    background: rgba(255, 252, 247, 0.7);
    border-radius: var(--lib-radius-md);
    padding: var(--lib-space-sm);
}

.upload-files-wrap /deep/ .el-upload-list__item {
    border: 1px solid var(--lib-border);
    border-radius: 8px;
    background: var(--lib-bg-surface);
}

.upload-files-wrap /deep/ .el-upload-list__item-name {
    color: var(--lib-text-primary);
}

.upload-files-wrap /deep/ .el-upload-list__item-status-label,
.upload-files-wrap /deep/ .el-upload-list__item .el-icon-close {
    color: var(--lib-text-secondary);
}

.upload-files-wrap /deep/ .el-button--primary {
    background: var(--lib-accent);
    border-color: var(--lib-accent);
    border-radius: var(--lib-radius-md);
}

.upload-files-wrap /deep/ .el-button--primary:hover,
.upload-files-wrap /deep/ .el-button--primary:focus {
    background: color-mix(in srgb, var(--lib-accent) 88%, #ffffff 12%);
    border-color: color-mix(in srgb, var(--lib-accent) 88%, #ffffff 12%);
}

.upload-tip {
    color: var(--lib-text-secondary);
}

.upload-tip-highlight {
    padding-left: var(--lib-space-sm);
    color: var(--lib-wood);
    font-weight: 600;
}
</style>

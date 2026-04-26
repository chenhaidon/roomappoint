<template>
    <div class="upload-files-wrap">
        <el-upload :action="uploadUrl" list-type="picture-card" :show-file-list="true" :on-success="handleUploadSuccess"
            :on-remove="handleRemove" :file-list="fileList" accept=".jpg,.png,.jpeg" :limit="limit" :multiple="true">
            <i class="el-icon-plus"></i>

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

.upload-files-wrap /deep/ .el-upload--picture-card {
    background: var(--lib-bg-surface) !important;
    border-color: var(--lib-border) !important;
    border-radius: var(--lib-radius-md);
    color: var(--lib-text-secondary);
    transition: border-color .2s ease, color .2s ease;
}

.upload-files-wrap /deep/ .el-upload--picture-card:hover {
    border-color: var(--lib-accent) !important;
    color: var(--lib-accent);
}

.upload-files-wrap /deep/ .el-upload-list--picture-card .el-upload-list__item {
    border: 1px solid var(--lib-border);
    border-radius: var(--lib-radius-md);
    background: var(--lib-bg-surface);
}

.upload-files-wrap /deep/ .el-upload-list__item-actions {
    border-radius: var(--lib-radius-md);
}
</style>

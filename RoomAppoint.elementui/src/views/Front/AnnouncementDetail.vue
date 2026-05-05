<template>
    <div v-if="detail" class="margin-top-lg margin-bottom-lg">
        <el-page-header class="card" @back="goBack" :content="detail.Title"></el-page-header>
        <div class="card margin-top-lg notice-detail-wrap">
            <div class="notice-title">{{ detail.Title }}</div>
            <div class="notice-time">发布时间：{{ detail.PublishTime || detail.CreationTime }}</div>
            <img v-if="detail.Cover" class="notice-cover" :src="NormalizeImage(detail.Cover)">
            <div class="notice-summary" v-if="detail.Summary">{{ detail.Summary }}</div>
            <div class="notice-content" v-html="detail.Content"></div>
        </div>
    </div>
</template>

<script>
import { ReplaceImageHttp } from "@/utils/comm";

export default {
    name: "AnnouncementDetail",
    data() {
        return {
            detail: null,
            id: null,
        }
    },
    created() {
        this.id = this.$route.query.Id;
        this.GetDetail();
    },
    methods: {
        NormalizeImage(value) {
            if (!value) {
                return "";
            }
            let raw = Array.isArray(value) ? value[0] : String(value);
            raw = raw.trim();
            if (raw.startsWith("[") && raw.endsWith("]")) {
                try {
                    const arr = JSON.parse(raw);
                    raw = Array.isArray(arr) ? (arr[0] || "") : raw;
                } catch (e) {
                }
            }
            raw = String(raw).split(",")[0].trim().replace(/^['\"]|['\"]$/g, "");
            return ReplaceImageHttp(raw);
        },
        async GetDetail() {
            let { Data } = await this.$Post('/Announcement/FrontGet', { Id: this.id });
            this.detail = Data;
        },
        goBack() {
            this.$router.go(-1)
        }
    }
}
</script>

<style scoped>
.notice-detail-wrap {
    padding: var(--lib-space-lg);
}

.notice-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--lib-text-primary);
}

.notice-time {
    margin-top: 8px;
    color: var(--lib-text-secondary);
}

.notice-cover {
    width: 100%;
    border-radius: 12px;
    margin-top: var(--lib-space-md);
}

.notice-summary {
    margin-top: var(--lib-space-md);
    color: var(--lib-text-secondary);
    background: #faf7f2;
    border-left: 4px solid var(--lib-wood);
    padding: var(--lib-space-sm);
}

.notice-content {
    margin-top: var(--lib-space-md);
    line-height: 2;
}
</style>

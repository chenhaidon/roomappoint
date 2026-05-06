<template>
    <div class="anno-page">
        <!-- 顶部 -->
        <div class="page-header">
            <div class="header-back" @click="$router.go(-1)">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">公告</div>
        </div>

        <!-- 搜索+筛选 -->
        <div class="filter-bar">
            <el-input v-model="keyword" placeholder="搜索公告" size="small" clearable
                prefix-icon="el-icon-search" class="filter-input"></el-input>
            <div class="filter-tags">
                <span class="filter-tag" :class="{ active: activeType === '' }" @click="activeType = ''">全部</span>
                <span class="filter-tag" :class="{ active: activeType === 'top' }" @click="activeType = 'top'">置顶</span>
            </div>
        </div>

        <!-- 公告列表 -->
        <div class="anno-list" v-if="filteredData.length > 0">
            <div class="anno-card" v-for="item in filteredData" :key="item.Id" @click="ToDetail(item)">
                <div class="anno-cover" v-if="item.Cover">
                    <img :src="NormalizeImage(item.Cover)" :alt="item.Title">
                </div>
                <div class="anno-body">
                    <div class="anno-tags">
                        <span class="anno-tag tag-top" v-if="item.IsTop == 1">置顶</span>
                        <span class="anno-tag tag-new" v-if="isNew(item)">新公告</span>
                    </div>
                    <div class="anno-title">{{ item.Title }}</div>
                    <div class="anno-summary" v-if="item.Summary">{{ item.Summary }}</div>
                    <div class="anno-meta">
                        <span class="anno-time"><i class="el-icon-time"></i> {{ formatTime(item.PublishTime || item.CreationTime) }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else>
            <div class="empty-icon"><i class="el-icon-bell"></i></div>
            <div class="empty-text">暂无公告</div>
            <div class="empty-hint">有新公告时会在这里通知你</div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore" @click="loadMore">
            加载更多
        </div>
    </div>
</template>

<script>
import { ReplaceImageHttp } from "@/utils/comm";

export default {
    name: "AnnouncementList",
    data() {
        return {
            allData: [],
            page: 1,
            limit: 10,
            hasMore: true,
            keyword: '',
            activeType: '',
        };
    },
    computed: {
        filteredData() {
            let list = this.allData;
            if (this.activeType === 'top') {
                list = list.filter(x => x.IsTop == 1);
            }
            if (this.keyword) {
                const kw = this.keyword.toLowerCase();
                list = list.filter(x =>
                    (x.Title && x.Title.toLowerCase().includes(kw)) ||
                    (x.Summary && x.Summary.toLowerCase().includes(kw))
                );
            }
            return list;
        }
    },
    created() {
        this.loadData();
    },
    methods: {
        NormalizeImage(value) {
            if (!value) return "";
            let raw = Array.isArray(value) ? value[0] : String(value);
            raw = raw.trim();
            if (raw.startsWith("[") && raw.endsWith("]")) {
                try { raw = JSON.parse(raw)[0] || raw; } catch (e) {}
            }
            raw = String(raw).split(",")[0].trim().replace(/^['\"]|['\"]$/g, "");
            return ReplaceImageHttp(raw);
        },
        async loadData(append = false) {
            try {
                const res = await this.$Post('/Announcement/FrontList', { Page: this.page, Limit: this.limit });
                if (res && res.Data) {
                    const items = res.Data.Items || [];
                    if (append) {
                        this.allData = [...this.allData, ...items];
                    } else {
                        this.allData = items;
                    }
                    this.hasMore = items.length >= this.limit;
                }
            } catch (e) {}
        },
        loadMore() {
            this.page++;
            this.loadData(true);
        },
        formatTime(t) {
            if (!t) return '';
            return t.split(' ')[0];
        },
        isNew(item) {
            if (!item.CreationTime) return false;
            const created = new Date(item.CreationTime).getTime();
            const now = Date.now();
            return (now - created) < 3 * 24 * 3600 * 1000;
        },
        ToDetail(item) {
            this.$router.push({ path: "/Front/AnnouncementDetail", query: { Id: item.Id } });
        }
    }
};
</script>

<style scoped>
.anno-page {
    max-width: 600px;
    margin: 0 auto;
    min-height: 100vh;
    background: #f5f6f8;
    padding-bottom: 24px;
}

/* 顶部 */
.page-header {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #fff;
}
.header-back {
    font-size: 20px;
    padding: 8px;
    cursor: pointer;
    color: #333;
}
.header-title {
    font-size: 17px;
    font-weight: 600;
    color: #1a1a1a;
    margin-left: 4px;
}

/* 筛选栏 */
.filter-bar {
    padding: 12px 16px;
    background: #fff;
    border-bottom: 1px solid #f0f0f0;
}
.filter-input {
    margin-bottom: 10px;
}
.filter-tags {
    display: flex;
    gap: 8px;
}
.filter-tag {
    padding: 4px 14px;
    border-radius: 14px;
    font-size: 12px;
    color: #888;
    background: #f5f5f5;
    cursor: pointer;
    transition: all 0.2s;
}
.filter-tag.active {
    background: #3A5F73;
    color: #fff;
}

/* 公告列表 */
.anno-list {
    padding: 16px;
}
.anno-card {
    background: #fff;
    border-radius: 16px;
    overflow: hidden;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    transition: all 0.2s;
    cursor: pointer;
}
.anno-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.anno-cover {
    width: 100%;
    height: 160px;
    overflow: hidden;
}
.anno-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.anno-body {
    padding: 16px;
}
.anno-tags {
    display: flex;
    gap: 6px;
    margin-bottom: 8px;
}
.anno-tag {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 8px;
    font-weight: 500;
}
.tag-top {
    background: #fff3e0;
    color: #e65100;
}
.tag-new {
    background: #e8f5e9;
    color: #2e7d32;
}
.anno-title {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 6px;
    line-height: 1.4;
}
.anno-summary {
    font-size: 13px;
    color: #999;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-bottom: 10px;
}
.anno-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.anno-time {
    font-size: 12px;
    color: #ccc;
}
.anno-time i {
    margin-right: 2px;
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 24px;
}
.empty-icon {
    font-size: 48px;
    color: #ddd;
    margin-bottom: 12px;
}
.empty-text {
    font-size: 16px;
    color: #999;
    margin-bottom: 6px;
}
.empty-hint {
    font-size: 13px;
    color: #ccc;
}

/* 加载更多 */
.load-more {
    text-align: center;
    padding: 20px;
    font-size: 13px;
    color: #3A5F73;
    cursor: pointer;
}

/* 动效 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
}
.anno-page { animation: fadeUp 0.3s ease; }
</style>

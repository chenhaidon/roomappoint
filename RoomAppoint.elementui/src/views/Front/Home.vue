<template>
    <div class="home-page">
        <!-- 顶部状态区 -->
        <div class="top-status top--enter">
            <div class="top-greeting">
                <span class="top-hello">你好，{{ homeData.UserName || '同学' }}</span>
                <span class="top-status-text" v-if="homeData.AppointStatus === 2">
                    你正在学习 · {{ homeData.RoomName }} {{ homeData.SeatName }}
                </span>
                <span class="top-status-text" v-else-if="homeData.AppointStatus === 1">
                    待使用 · {{ homeData.RoomName }} {{ homeData.SeatName }}
                </span>
                <span class="top-status-text" v-else>当前状态：未入座</span>
            </div>
            <div class="top-study" v-if="homeData.TodayStudyMinutes > 0">
                已学习：{{ formatStudyTime(homeData.TodayStudyMinutes) }}
            </div>
        </div>

        <!-- Hero区 -->
        <div class="hero-section hero--enter">
            <div class="hero-glow"></div>
            <div class="hero-content">
                <h1 class="hero-title">开始你的专注时刻</h1>
                <p class="hero-subtitle">选一个位置，让自己安静下来</p>
                <div class="hero-actions">
                    <el-button class="hero-btn hero-btn--primary" type="primary" @click="handleMainAction">
                        {{ homeData.AppointStatus >= 1 ? '继续学习' : '进入自习室' }}
                    </el-button>
                    <el-button class="hero-btn hero-btn--accent" @click="handleAutoSelect">
                        <i class="el-icon-magic-stick"></i> 帮我选座
                    </el-button>
                </div>
                <div class="hero-recent" v-if="homeData.RecentRoomName && homeData.AppointStatus === 0">
                    <i class="el-icon-time"></i> 你常用：{{ homeData.RecentRoomName }}
                </div>
            </div>
        </div>

        <!-- 推荐自习室 -->
        <div class="recommend-section" v-if="homeData.RecommendedRooms && homeData.RecommendedRooms.length > 0 && homeData.AppointStatus === 0">
            <div class="section-header recommend--enter" style="--delay: 120ms">
                <span class="section-title">推荐给你</span>
            </div>
            <div class="recommend-list">
                <div class="recommend-card recommend--enter"
                     v-for="(room, index) in homeData.RecommendedRooms" :key="room.Id"
                     :style="{ '--delay': (180 + index * 60) + 'ms' }"
                     @click="ToRoom(room)">
                    <div class="recommend-rank">{{ index + 1 }}</div>
                    <div class="recommend-info">
                        <span class="recommend-name">{{ room.Name }}</span>
                        <span class="recommend-tag">{{ room.RecommendTag }}</span>
                    </div>
                    <div class="recommend-seats" :class="seatLevel(room)">
                        空位 {{ room.AvailableSeats }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 筛选区 -->
        <div class="filter-section filter--enter" style="--delay: 300ms" v-if="homeData.AppointStatus === 0">
            <div class="filter-bar">
                <el-input
                    v-model="searchText"
                    placeholder="搜索自习室名称"
                    prefix-icon="el-icon-search"
                    size="small"
                    clearable
                    class="filter-search">
                </el-input>
                <div class="filter-tags">
                    <span class="filter-tag" :class="{ active: filterAvail === '' }" @click="filterAvail = ''">全部</span>
                    <span class="filter-tag" :class="{ active: filterAvail === 'many' }" @click="filterAvail = 'many'">空位多</span>
                    <span class="filter-tag" :class="{ active: filterAvail === 'some' }" @click="filterAvail = 'some'">有空位</span>
                </div>
            </div>
        </div>

        <!-- 自习室列表 -->
        <div class="room-section" ref="roomSection" v-if="homeData.AppointStatus === 0">
            <div class="room-list">
                <div class="room-card room--enter"
                     v-for="(room, index) in filteredRooms" :key="room.Id"
                     :style="{ '--delay': (360 + index * 60) + 'ms' }"
                     @click="ToRoom(room)">
                    <div class="room-cover" v-if="room.Cover">
                        <img :src="NormalizeImage(room.Cover)" :alt="room.Name">
                    </div>
                    <div class="room-body">
                        <div class="room-header">
                            <span class="room-name">{{ room.Name }}</span>
                            <span class="room-avail-badge" :class="seatLevel(room)">
                                {{ seatLevelText(room) }}
                            </span>
                        </div>
                        <div class="room-stats">
                            <span class="room-stat">
                                <i class="el-icon-user"></i> {{ room.TotalSeats - room.AvailableSeats }}/{{ room.TotalSeats }}
                            </span>
                            <span class="room-stat">
                                剩余 <strong>{{ room.AvailableSeats }}</strong> 座
                            </span>
                        </div>
                        <el-button class="room-btn" size="small" @click.stop="ToRoom(room)">查看座位</el-button>
                    </div>
                </div>
            </div>
            <el-empty v-if="filteredRooms.length === 0" description="没有匹配的自习室"></el-empty>
        </div>

        <!-- 已入座时显示当前座位信息 -->
        <div class="seated-section seated--enter" style="--delay: 200ms" v-if="homeData.AppointStatus >= 1">
            <div class="seated-card">
                <div class="seated-icon">
                    <i class="el-icon-location"></i>
                </div>
                <div class="seated-info">
                    <div class="seated-room">{{ homeData.RoomName }}</div>
                    <div class="seated-seat">{{ homeData.SeatName }}</div>
                </div>
                <el-button type="primary" size="small" class="seated-btn" @click="ToAppointRecord">
                    查看详情
                </el-button>
            </div>
        </div>

        <!-- 公告区 -->
        <div class="announce-section announce--enter" style="--delay: 500ms"
             v-if="homeData.Announcements && homeData.Announcements.length > 0">
            <div class="announce-header">
                <span class="announce-title">公告</span>
                <el-button type="text" class="announce-more" @click="ToAnnouncementList">查看更多</el-button>
            </div>
            <div class="announce-list">
                <div class="announce-item" v-for="item in homeData.Announcements" :key="item.Id"
                     @click="ToAnnouncementDetail(item)">
                    <el-tag v-if="item.IsTop == 1" size="mini" type="danger" class="announce-tag">置顶</el-tag>
                    <span class="announce-text">{{ item.Title }}</span>
                    <span class="announce-time">{{ item.PublishTime || item.CreationTime }}</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { mapState } from 'vuex';
import { ReplaceImageHttp } from "@/utils/comm";
export default {
    data() {
        return {
            searchText: '',
            filterAvail: '',
            homeData: {
                UserName: '',
                AppointStatus: 0,
                RoomName: '',
                SeatName: '',
                BeginTime: '',
                TodayStudyMinutes: 0,
                Balance: 0,
                TotalInUse: 0,
                TotalAvailable: 0,
                RecommendedRooms: [],
                Rooms: [],
                RecentRoomName: '',
                RecentRoomId: null,
                Announcements: [],
            }
        }
    },
    computed: {
        ...mapState(['Token']),
        filteredRooms() {
            let rooms = this.homeData.Rooms || [];
            if (this.searchText) {
                const kw = this.searchText.toLowerCase();
                rooms = rooms.filter(r => r.Name && r.Name.toLowerCase().includes(kw));
            }
            if (this.filterAvail === 'many') {
                rooms = rooms.filter(r => r.TotalSeats > 0 && (r.AvailableSeats / r.TotalSeats) > 0.5);
            } else if (this.filterAvail === 'some') {
                rooms = rooms.filter(r => r.AvailableSeats > 0);
            }
            return rooms;
        }
    },
    created() {
        this.FetchHomeData();
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
        async FetchHomeData() {
            try {
                const res = await this.$Post("/Home/GetHomeData", {});
                if (res && res.Success && res.Data) {
                    this.homeData = res.Data;
                }
            } catch (e) {}
        },
        formatStudyTime(mins) {
            if (!mins || mins <= 0) return '0min';
            const h = Math.floor(mins / 60);
            const m = mins % 60;
            return h > 0 ? `${h}h ${m}min` : `${m}min`;
        },
        seatLevel(room) {
            if (!room.TotalSeats) return 'level-empty';
            const ratio = room.AvailableSeats / room.TotalSeats;
            if (ratio > 0.5) return 'level-many';
            if (ratio > 0.2) return 'level-some';
            return 'level-few';
        },
        seatLevelText(room) {
            if (!room.AvailableSeats) return '已满';
            const ratio = room.TotalSeats > 0 ? room.AvailableSeats / room.TotalSeats : 0;
            if (ratio > 0.5) return '空位多';
            if (ratio > 0.2) return '有空位';
            return '快满';
        },
        handleMainAction() {
            console.log('handleMainAction called, status:', this.homeData.AppointStatus);
            if (this.homeData.AppointStatus >= 1) {
                this.ToAppointRecord();
            } else {
                this.$nextTick(() => {
                    const el = document.querySelector('.room-section');
                    if (el) {
                        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    }
                });
            }
        },
        async handleAutoSelect() {
            try {
                const res = await this.$Post("/Home/AutoSelectSeat", {});
                if (res && res.Success && res.Data && res.Data.Success) {
                    const d = res.Data;
                    this.$router.push({
                        path: "/Front/Room",
                        query: { RoomId: d.RoomId, SeatId: d.SeatId }
                    });
                } else {
                    this.$message.warning((res.Data && res.Data.Msg) || '暂无可用座位');
                }
            } catch (e) {
                this.$message.error('操作失败');
            }
        },
        ToRoomList() {
            this.$router.push({ path: "/Front/Room" });
        },
        ToRoom(room) {
            this.$router.push({ path: "/Front/Room", query: { RoomId: room.Id } });
        },
        ToAppointRecord() {
            this.$router.push({ path: "/Front/AppointRecordList" });
        },
        ToAnnouncementList() {
            this.$router.push({ path: "/Front/AnnouncementList" });
        },
        ToAnnouncementDetail(item) {
            this.$router.push({ path: "/Front/AnnouncementDetail", query: { Id: item.Id } });
        },
    }
}
</script>

<style scoped>
.home-page {
    --home-primary: #3A5F73;
    --home-primary-dark: #2F4E5C;
    --home-accent: #E6B980;
    --home-card: #FFFEF7;
    --ease-out: cubic-bezier(0.22, 1, 0.36, 1);

    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 24px;
    min-height: calc(100vh - 60px);
}

@keyframes fadeUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: none; }
}

@keyframes itemIn {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: none; }
}

/* 顶部状态 */
.top-status {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0;
}

.top--enter { animation: fadeUp 400ms var(--ease-out); }

.top-greeting { display: flex; flex-direction: column; gap: 2px; }
.top-hello { font-size: 18px; font-weight: 700; color: #2A3A42; }
.top-status-text { font-size: 13px; color: #8B7355; }
.top-study { font-size: 13px; color: var(--home-primary); font-weight: 600; }

/* Hero区 */
.hero-section {
    height: 240px;
    border-radius: 16px;
    background: linear-gradient(135deg, var(--home-primary), var(--home-primary-dark));
    color: #fff;
    padding: 32px;
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
}

.hero--enter { animation: fadeUp 520ms var(--ease-out); }

.hero-glow {
    position: absolute;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(230, 185, 128, 0.2), transparent);
    top: 20%;
    right: 10%;
    filter: blur(50px);
    pointer-events: none;
    animation: glow 6s ease-in-out infinite;
}

@keyframes glow {
    0%, 100% { opacity: 0.6; transform: scale(1); }
    50% { opacity: 1; transform: scale(1.08); }
}

.hero-content { position: relative; z-index: 1; }
.hero-title { font-size: 26px; font-weight: 700; margin: 0 0 6px; letter-spacing: 2px; }
.hero-subtitle { font-size: 14px; opacity: 0.7; margin: 0 0 24px; }

.hero-actions { display: flex; gap: 12px; flex-wrap: wrap; }

.hero-btn {
    height: 44px;
    border-radius: 12px;
    font-weight: 600;
    letter-spacing: 2px;
    transition: all 200ms var(--ease-out);
}

.hero-btn--primary {
    background: linear-gradient(135deg, var(--home-accent), #D4A56A);
    border: none;
    box-shadow: 0 4px 16px rgba(230, 185, 128, 0.35);
    color: #fff;
}

.hero-btn--primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 24px rgba(230, 185, 128, 0.45);
    filter: brightness(1.05);
}

.hero-btn--accent {
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.25);
    color: #fff;
}

.hero-btn--accent:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
}

.hero-recent {
    margin-top: 14px;
    font-size: 12px;
    opacity: 0.6;
}

.hero-recent i { margin-right: 4px; }

/* 推荐区 */
.recommend-section { display: flex; flex-direction: column; gap: 12px; }

.section-header { }
.section-title { font-size: 16px; font-weight: 700; color: #2A3A42; }

.recommend-list { display: flex; flex-direction: column; gap: 10px; }

.recommend-card {
    display: flex;
    align-items: center;
    gap: 14px;
    background: var(--home-card);
    border-radius: 12px;
    padding: 14px 18px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
    cursor: pointer;
    transition: all 200ms var(--ease-out);
}

.recommend--enter {
    opacity: 0;
    transform: translateY(12px);
    animation: itemIn 200ms var(--ease-out) forwards;
    animation-delay: var(--delay, 0ms);
}

.recommend-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

.recommend-rank {
    width: 28px;
    height: 28px;
    border-radius: 8px;
    background: linear-gradient(135deg, var(--home-primary), var(--home-primary-dark));
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    flex-shrink: 0;
}

.recommend-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.recommend-name { font-size: 15px; font-weight: 600; color: #2A3A42; }
.recommend-tag { font-size: 11px; color: #8B7355; }

.recommend-seats {
    font-size: 13px;
    font-weight: 600;
    padding: 4px 10px;
    border-radius: 8px;
    flex-shrink: 0;
}

/* 筛选区 */
.filter-section {}
.filter--enter {
    opacity: 0;
    transform: translateY(12px);
    animation: itemIn 200ms var(--ease-out) forwards;
    animation-delay: var(--delay, 0ms);
}

.filter-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
}

.filter-search {
    width: 220px;
}

.filter-search /deep/ .el-input__inner {
    border-radius: 10px;
    background: var(--home-card);
    border-color: #E8E3D9;
}

.filter-tags {
    display: flex;
    gap: 8px;
}

.filter-tag {
    font-size: 12px;
    padding: 5px 14px;
    border-radius: 20px;
    background: var(--home-card);
    color: #8B7355;
    cursor: pointer;
    transition: all 200ms;
    border: 1px solid #E8E3D9;
}

.filter-tag:hover { border-color: var(--home-primary); color: var(--home-primary); }
.filter-tag.active {
    background: var(--home-primary);
    color: #fff;
    border-color: var(--home-primary);
}

/* 自习室列表 */
.room-section {}
.room-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
}

.room-card {
    background: var(--home-card);
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    transition: all 200ms var(--ease-out);
}

.room--enter {
    opacity: 0;
    transform: translateY(12px);
    animation: itemIn 200ms var(--ease-out) forwards;
    animation-delay: var(--delay, 0ms);
}

.room-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 36px rgba(0, 0, 0, 0.1);
}

.room-cover {
    height: 140px;
    overflow: hidden;
}

.room-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.room-body {
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.room-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.room-name {
    font-size: 15px;
    font-weight: 700;
    color: #2A3A42;
}

.room-avail-badge {
    font-size: 11px;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: 6px;
}

.room-stats {
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: #A0AAB4;
}

.room-stats i { margin-right: 2px; }
.room-stat strong { color: var(--home-primary); }

.room-btn {
    align-self: flex-start;
    border-radius: 8px;
    font-size: 12px;
    margin-top: 4px;
    color: var(--home-primary);
    border-color: var(--home-primary);
    background: transparent;
}

.room-btn:hover {
    background: var(--home-primary);
    color: #fff;
}

/* 空位颜色 */
.level-many { background: rgba(122, 158, 130, 0.12); color: #5A8A62; }
.level-some { background: rgba(230, 185, 128, 0.15); color: #B8860B; }
.level-few { background: rgba(200, 80, 80, 0.1); color: #C85050; }
.level-empty { background: rgba(160, 160, 160, 0.1); color: #999; }

/* 已入座 */
.seated-section {}
.seated--enter {
    opacity: 0;
    transform: translateY(12px);
    animation: itemIn 200ms var(--ease-out) forwards;
    animation-delay: var(--delay, 0ms);
}

.seated-card {
    display: flex;
    align-items: center;
    gap: 16px;
    background: var(--home-card);
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
    border-left: 4px solid var(--home-primary);
}

.seated-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: rgba(58, 95, 115, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.seated-icon i { font-size: 24px; color: var(--home-primary); }
.seated-info { flex: 1; }
.seated-room { font-size: 16px; font-weight: 700; color: #2A3A42; }
.seated-seat { font-size: 13px; color: #8B7355; margin-top: 2px; }
.seated-btn { border-radius: 8px; }

/* 公告区 */
.announce-section {
    background: var(--home-card);
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.announce--enter {
    opacity: 0;
    transform: translateY(12px);
    animation: itemIn 200ms var(--ease-out) forwards;
    animation-delay: var(--delay, 0ms);
}

.announce-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
}

.announce-title { font-size: 16px; font-weight: 700; color: #2A3A42; }
.announce-more { color: var(--home-accent); font-size: 13px; font-weight: 600; }

.announce-list { display: flex; flex-direction: column; }

.announce-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 0;
    border-bottom: 1px dashed #EDE9DF;
    cursor: pointer;
    transition: background 200ms;
}

.announce-item:last-child { border-bottom: none; }
.announce-item:hover { background: rgba(230, 185, 128, 0.04); }
.announce-tag { flex-shrink: 0; }

.announce-text {
    flex: 1;
    font-size: 14px;
    color: #2A3A42;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.announce-time {
    font-size: 12px;
    color: #A0AAB4;
    white-space: nowrap;
    flex-shrink: 0;
}

/* 移动端 */
@media (max-width: 768px) {
    .home-page { padding: 16px; gap: 20px; }
    .hero-section { height: 200px; padding: 24px; }
    .hero-title { font-size: 22px; }
    .hero-subtitle { font-size: 13px; margin-bottom: 18px; }
    .filter-bar { flex-direction: column; align-items: stretch; }
    .filter-search { width: 100%; }
    .room-list { grid-template-columns: 1fr; }
    .top-status { flex-direction: column; align-items: flex-start; gap: 4px; }
}
</style>

<template>
    <div class="room-page" v-if="RoomDetail">
        <!-- 顶部信息 -->
        <div class="room-header">
            <div class="header-back" @click="goBack">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-info">
                <div class="header-title">{{ RoomDetail.Name }}</div>
                <div class="header-meta">
                    <span>{{ roomDesc }}</span>
                    <span class="meta-sep">·</span>
                    <span>{{ occupiedCount }}/{{ totalSeats }}人</span>
                    <span class="meta-sep">·</span>
                    <span class="meta-avail">空位{{ availableCount }}</span>
                </div>
            </div>
        </div>

        <!-- 日期选择 -->
        <div class="date-bar">
            <div class="date-item" v-for="d in DateList" :key="d"
                 :class="{ active: SelectDate === d }"
                 @click="selectDate(d)">
                {{ formatDate(d) }}
            </div>
        </div>

        <!-- 时段选择 -->
        <div class="period-bar">
            <div class="period-item" :class="{ active: period === 1 }" @click="period = 1">上午</div>
            <div class="period-item" :class="{ active: period === 2 }" @click="period = 2">下午</div>
            <div class="period-item" :class="{ active: period === 3 }" @click="period = 3">夜晚</div>
        </div>

        <!-- 分区导航 -->
        <div class="zone-bar" v-if="zones.length > 1">
            <div class="zone-item" v-for="z in zones" :key="z.name"
                 :class="{ active: activeZone === z.name }"
                 @click="activeZone = z.name">
                {{ z.name }}
                <span class="zone-count">{{ z.availableCount }}</span>
            </div>
        </div>

        <!-- 推荐提示 -->
        <div class="recommend-tip" v-if="recommendSeat">
            <i class="el-icon-star-on"></i>
            已为你推荐：{{ activeZone }} · {{ recommendSeat.No }}
        </div>

        <!-- 座位图 -->
        <div class="seat-grid-wrap">
            <div class="seat-grid" v-if="zoneSeats.length > 0">
                <div class="seat-row" v-for="(row, ri) in zoneSeats" :key="ri">
                    <div class="seat" v-for="(seat, ci) in row" :key="ci"
                         :class="seatClass(seat)"
                         @click="selectSeat(seat)">
                        <template v-if="seat && seat.Id">
                            <span class="seat-no">{{ seat.No }}</span>
                            <i class="el-icon-star-on seat-star" v-if="isRecommend(seat)"></i>
                        </template>
                    </div>
                </div>
            </div>
            <el-empty v-else description="当前区域无可用座位"></el-empty>
        </div>

        <!-- 图例 -->
        <div class="legend">
            <div class="legend-item"><span class="legend-dot available"></span>可选</div>
            <div class="legend-item"><span class="legend-dot occupied"></span>已占</div>
            <div class="legend-item"><span class="legend-dot selected"></span>已选</div>
            <div class="legend-item"><span class="legend-dot recommend"></span>推荐</div>
        </div>

        <!-- 快满提示 -->
        <div class="warn-tip" v-if="availableCount > 0 && availableCount <= 5">
            <i class="el-icon-warning"></i> 仅剩少量座位
        </div>

        <!-- 底部确认栏 -->
        <div class="bottom-bar" v-if="selectedSeat">
            <div class="bar-info">
                <span class="bar-label">已选：</span>
                <span class="bar-seat">{{ selectedSeat.No }}（{{ activeZone }}）</span>
            </div>
            <el-button type="primary" class="bar-btn" @click="confirmSeat">确认入座</el-button>
        </div>

        <!-- 一键选座 -->
        <div class="quick-bar" v-else>
            <el-button type="primary" class="quick-btn" @click="quickSelect">
                <i class="el-icon-magic-stick"></i> 帮我选座
            </el-button>
        </div>
    </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { ReplaceImageHttp } from "@/utils/comm";

export default {
    name: "Room",
    computed: {
        ...mapGetters(["UserInfo", "Token", "UserId"]),
        // 当前时段的座位数据
        currentSeats() {
            if (!this.SeatArrange) return [];
            if (this.period === 1) return this.SeatArrange.AmSeatDtoList || [];
            if (this.period === 2) return this.SeatArrange.PmSeatDtoList || [];
            return this.SeatArrange.NmSeatDtoList || [];
        },
        // 所有有效座位（非空）
        allSeats() {
            const list = [];
            for (const row of this.currentSeats) {
                for (const s of row) {
                    if (s && s.Id) list.push(s);
                }
            }
            return list;
        },
        totalSeats() { return this.allSeats.length; },
        occupiedCount() { return this.allSeats.filter(s => s.IsOccupy).length; },
        availableCount() { return this.allSeats.filter(s => !s.IsOccupy && !s.IsMaintain).length; },
        roomDesc() {
            const name = this.RoomDetail.Name || '';
            if (name.includes('静音')) return '极安静';
            if (name.includes('讨论')) return '可讨论';
            return '安静';
        },
        // 分区逻辑：按行分为3个区
        zones() {
            const maxRow = this.currentSeats.length;
            if (maxRow === 0) return [];
            const third = Math.ceil(maxRow / 3);
            const result = [];
            const makeZone = (name, startRow, endRow) => {
                const seats = [];
                for (let r = startRow; r <= endRow && r <= maxRow; r++) {
                    const row = this.currentSeats[r - 1] || [];
                    for (const s of row) {
                        if (s && s.Id) seats.push(s);
                    }
                }
                const avail = seats.filter(s => !s.IsOccupy && !s.IsMaintain).length;
                return { name, startRow, endRow, seats, availableCount: avail };
            };
            result.push(makeZone('窗边区', 1, third));
            if (maxRow > third) result.push(makeZone('中间区', third + 1, third * 2));
            if (maxRow > third * 2) result.push(makeZone('靠墙区', third * 2 + 1, maxRow));
            return result;
        },
        // 当前分区的座位（二维数组）
        zoneSeats() {
            const zone = this.zones.find(z => z.name === this.activeZone);
            if (!zone) return [];
            const rows = [];
            for (let r = zone.startRow; r <= zone.endRow; r++) {
                const row = this.currentSeats[r - 1] || [];
                rows.push(row);
            }
            return rows;
        },
        // 推荐座位：中间区第一个空位
        recommendSeat() {
            const midZone = this.zones.find(z => z.name === '中间区') || this.zones[0];
            if (!midZone) return null;
            return midZone.seats.find(s => !s.IsOccupy && !s.IsMaintain) || null;
        }
    },
    data() {
        return {
            RoomId: undefined,
            RoomDetail: null,
            DateList: [],
            SeatArrange: null,
            SelectDate: null,
            period: 1,
            activeZone: '中间区',
            selectedSeat: null,
        };
    },
    created() {
        this.RoomId = this.$route.query.RoomId;
        this.GetRoomApi();
        this.GetSevenDaysApi();
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
        async GetRoomApi() {
            const { Data } = await this.$Post("/Room/Get", { Id: this.RoomId });
            this.RoomDetail = Data;
        },
        async GetSevenDaysApi() {
            const { Data } = await this.$Post("/Seat/GetSevenDays", {});
            this.DateList = Data;
            this.SelectDate = this.DateList[0];
            this.GetArrange();
        },
        async GetArrange() {
            const { Data } = await this.$Post("/Seat/GetArrange", {
                RoomId: this.RoomId,
                SelectDate: this.SelectDate + " 00:00:00"
            });
            this.SeatArrange = Data;
            this.selectedSeat = null;
            // 默认选中中间区
            const midZone = this.zones.find(z => z.name === '中间区');
            this.activeZone = midZone ? '中间区' : (this.zones[0] && this.zones[0].name) || '';
            // 自动选中推荐座位
            if (this.recommendSeat) {
                this.selectedSeat = this.recommendSeat;
            }
        },
        selectDate(d) {
            this.SelectDate = d;
            this.GetArrange();
        },
        formatDate(d) {
            if (!d) return '';
            const parts = d.split('-');
            if (parts.length >= 3) return parts[1] + '/' + parts[2];
            return d;
        },
        seatClass(seat) {
            if (!seat || !seat.Id) return 'seat-empty';
            if (this.selectedSeat && this.selectedSeat.Id === seat.Id) return 'seat-selected';
            if (seat.IsOccupy) return 'seat-occupied';
            if (seat.IsMaintain) return 'seat-occupied';
            if (this.isRecommend(seat)) return 'seat-recommend';
            return 'seat-available';
        },
        isRecommend(seat) {
            return this.recommendSeat && seat && seat.Id === this.recommendSeat.Id;
        },
        selectSeat(seat) {
            if (!seat || !seat.Id || seat.IsOccupy || seat.IsMaintain) return;
            if (!this.Token) {
                this.$message.warning("请先登录");
                return;
            }
            this.selectedSeat = seat;
        },
        goBack() {
            this.$router.go(-1);
        },
        async confirmSeat() {
            if (!this.selectedSeat) return;
            const type = this.period;
            const body = {
                RoomId: this.RoomId,
                UserId: this.UserId,
                SeatId: this.selectedSeat.Id,
                AppointDateType: type,
                AppointDate: this.SelectDate + " 00:00:00",
                RoomName: this.RoomDetail.Name,
                SeatNo: this.selectedSeat.No,
            };
            const { Success } = await this.$Post("/AppointRecord/CheckIsAbleAppoint", body);
            if (Success) {
                const tick = new Date().getTime();
                localStorage.setItem(tick, JSON.stringify(body));
                this.$router.push({ path: "/Front/ToOrder", query: { tick } });
            }
        },
        async quickSelect() {
            if (!this.Token) {
                this.$message.warning("请先登录");
                return;
            }
            try {
                const res = await this.$Post("/Home/AutoSelectSeat", {});
                if (res && res.Success && res.Data && res.Data.Success) {
                    const d = res.Data;
                    this.$router.push({
                        path: "/Front/ToOrder",
                        query: { RoomId: d.RoomId, SeatId: d.SeatId, auto: 1 }
                    });
                } else {
                    this.$message.warning(res.Msg || "暂无可用座位");
                }
            } catch (e) {
                this.$message.error("操作失败");
            }
        }
    }
};
</script>

<style scoped>
.room-page {
    max-width: 600px;
    margin: 0 auto;
    padding-bottom: 80px;
    min-height: 100vh;
    background: #f5f6f8;
}

/* 顶部 */
.room-header {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #fff;
}
.header-back {
    font-size: 20px;
    padding: 4px 8px;
    cursor: pointer;
    color: #333;
}
.header-info { flex: 1; margin-left: 8px; }
.header-title { font-size: 17px; font-weight: 600; color: #1a1a1a; }
.header-meta { font-size: 12px; color: #888; margin-top: 4px; }
.meta-sep { margin: 0 4px; }
.meta-avail { color: #3A5F73; font-weight: 500; }

/* 日期栏 */
.date-bar {
    display: flex;
    gap: 8px;
    padding: 12px 16px;
    background: #fff;
    overflow-x: auto;
    border-top: 1px solid #f0f0f0;
}
.date-item {
    flex-shrink: 0;
    padding: 6px 14px;
    border-radius: 16px;
    font-size: 13px;
    color: #666;
    background: #f5f5f5;
    cursor: pointer;
    transition: all 0.2s;
}
.date-item.active {
    background: #3A5F73;
    color: #fff;
}

/* 时段栏 */
.period-bar {
    display: flex;
    gap: 0;
    padding: 0 16px;
    background: #fff;
    border-top: 1px solid #f0f0f0;
}
.period-item {
    flex: 1;
    text-align: center;
    padding: 10px 0;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: all 0.2s;
}
.period-item.active {
    color: #3A5F73;
    border-bottom-color: #3A5F73;
    font-weight: 500;
}

/* 分区导航 */
.zone-bar {
    display: flex;
    gap: 8px;
    padding: 12px 16px;
    background: #fff;
    margin-top: 8px;
    overflow-x: auto;
}
.zone-item {
    flex-shrink: 0;
    padding: 6px 14px;
    border-radius: 16px;
    font-size: 13px;
    color: #666;
    background: #f5f5f5;
    cursor: pointer;
    transition: all 0.2s;
}
.zone-item.active {
    background: #3A5F73;
    color: #fff;
}
.zone-count {
    margin-left: 4px;
    font-size: 11px;
    opacity: 0.8;
}

/* 推荐提示 */
.recommend-tip {
    padding: 10px 16px;
    font-size: 13px;
    color: #3A5F73;
    background: #e8f0f5;
}
.recommend-tip i { margin-right: 4px; }

/* 座位图 */
.seat-grid-wrap {
    padding: 16px;
}
.seat-grid {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}
.seat-row {
    display: flex;
    gap: 8px;
}
.seat {
    width: 42px;
    height: 42px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
}
.seat-empty {
    width: 42px;
    height: 42px;
    visibility: hidden;
}
.seat-available { background: #DCEEE7; color: #2d6a4f; }
.seat-occupied { background: #F2D7D5; color: #b08080; cursor: not-allowed; }
.seat-selected {
    background: #3A5F73;
    color: #fff;
    transform: scale(1.1);
    box-shadow: 0 2px 8px rgba(58,95,115,0.3);
}
.seat-recommend { background: #DCEEE7; color: #2d6a4f; border: 2px solid #f0c040; }
.seat-no { font-size: 10px; }
.seat-star {
    position: absolute;
    top: -4px;
    right: -4px;
    font-size: 12px;
    color: #f0c040;
}

/* 图例 */
.legend {
    display: flex;
    gap: 16px;
    justify-content: center;
    padding: 8px 16px;
    font-size: 12px;
    color: #888;
}
.legend-item { display: flex; align-items: center; gap: 4px; }
.legend-dot {
    width: 12px;
    height: 12px;
    border-radius: 3px;
}
.legend-dot.available { background: #DCEEE7; }
.legend-dot.occupied { background: #F2D7D5; }
.legend-dot.selected { background: #3A5F73; }
.legend-dot.recommend { background: #DCEEE7; border: 2px solid #f0c040; }

/* 快满提示 */
.warn-tip {
    text-align: center;
    padding: 8px;
    font-size: 13px;
    color: #e6a23c;
}

/* 底部确认栏 */
.bottom-bar {
    position: fixed;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    width: 90%;
    max-width: 560px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
    padding: 12px 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    z-index: 100;
}
.bar-info { font-size: 14px; }
.bar-label { color: #888; }
.bar-seat { color: #3A5F73; font-weight: 600; }
.bar-btn { border-radius: 20px; padding: 10px 24px; }

/* 一键选座栏 */
.quick-bar {
    position: fixed;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    width: 90%;
    max-width: 560px;
    z-index: 100;
}
.quick-btn {
    width: 100%;
    border-radius: 12px;
    padding: 14px;
    font-size: 15px;
    background: #3A5F73;
    border-color: #3A5F73;
}

/* 动效 */
@keyframes fadeUp {
    from { opacity: 0; transform: translateY(12px); }
    to { opacity: 1; transform: translateY(0); }
}
.room-page { animation: fadeUp 0.3s ease; }
</style>

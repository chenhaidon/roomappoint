<template>
    <div class="order-page">
        <!-- 顶部 -->
        <div class="order-header">
            <div class="header-back" @click="goBack">
                <i class="el-icon-arrow-left"></i>
            </div>
            <div class="header-title">确认你的座位</div>
        </div>

        <!-- 核心信息卡片 -->
        <div class="info-card">
            <div class="card-room">
                <span class="card-room-name">{{ roomName }}</span>
            </div>
            <div class="card-seat-no">{{ seatNo }}</div>
            <div class="card-time">
                <i class="el-icon-time"></i>
                {{ periodLabel }}（{{ periodTime }}）
            </div>
            <div class="card-date">{{ displayDate }}</div>
            <div class="card-change" @click="changeSeat">
                <i class="el-icon-refresh"></i> 更换座位
            </div>
        </div>

        <!-- 用户信息 -->
        <div class="user-card">
            <div class="user-field">
                <label>姓名</label>
                <el-input v-model="formData.Name" placeholder="请输入姓名" size="small" clearable></el-input>
            </div>
            <div class="user-field">
                <label>手机号</label>
                <el-input v-model="formData.Phone" placeholder="请输入手机号" size="small" clearable></el-input>
            </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="bottom-bar">
            <el-button class="bar-btn-back" @click="goBack">返回修改</el-button>
            <el-button class="bar-btn-confirm" type="primary" :loading="submitting" @click="ToOrder">
                {{ submitting ? '正在为你锁定座位…' : '确认入座' }}
            </el-button>
        </div>
    </div>
</template>

<script>
import { mapGetters } from 'vuex';

const PERIOD_MAP = {
    1: { label: '上午', time: '08:00–12:00' },
    2: { label: '下午', time: '14:00–18:00' },
    3: { label: '夜晚', time: '19:00–23:00' },
};

export default {
    computed: {
        ...mapGetters(['Token', 'UserInfo', 'UserId']),
        periodLabel() {
            return PERIOD_MAP[this.formData.AppointDateType]?.label || '';
        },
        periodTime() {
            return PERIOD_MAP[this.formData.AppointDateType]?.time || '';
        },
        displayDate() {
            if (!this.formData.AppointDate) return '';
            return this.formData.AppointDate.split(' ')[0];
        }
    },
    data() {
        return {
            formData: {},
            roomName: '',
            seatNo: '',
            submitting: false,
        };
    },
    created() {
        const tick = this.$route.query.tick;
        const jsonData = localStorage.getItem(tick);
        const data = JSON.parse(jsonData);
        this.formData = {
            RoomId: data.RoomId,
            SeatId: data.SeatId,
            UserId: data.UserId,
            AppointDateType: data.AppointDateType,
            AppointDate: data.AppointDate,
            AppointStatus: 1,
            Name: this.UserInfo?.Name || '',
            Phone: this.UserInfo?.PhoneNumber || '',
        };
        this.roomName = data.RoomName || '';
        this.seatNo = data.SeatNo || '';
    },
    methods: {
        goBack() {
            this.$router.go(-1);
        },
        changeSeat() {
            this.$router.push({ path: '/Front/Room', query: { RoomId: this.formData.RoomId } });
        },
        async ToOrder() {
            if (!this.formData.Name) {
                this.$message.warning('请输入姓名');
                return;
            }
            if (!this.formData.Phone || !/^1[3-9]\d{9}$/.test(this.formData.Phone)) {
                this.$message.warning('请输入正确的手机号');
                return;
            }
            this.submitting = true;
            try {
                const { Success, Msg } = await this.$Post('/AppointRecord/ToOrder', this.formData);
                if (Success) {
                    this.$router.push({ path: '/Front/AppointRecordList' });
                } else if (Msg && Msg.includes('已被预约')) {
                    this.$message.error('座位已被占用，请重新选座');
                    setTimeout(() => {
                        this.$router.push({ path: '/Front/Room', query: { RoomId: this.formData.RoomId } });
                    }, 1500);
                }
            } catch (e) {
                if (e.Msg && e.Msg.includes('已被预约')) {
                    this.$message.error('座位已被占用，请重新选座');
                    setTimeout(() => {
                        this.$router.push({ path: '/Front/Room', query: { RoomId: this.formData.RoomId } });
                    }, 1500);
                }
            } finally {
                this.submitting = false;
            }
        }
    }
};
</script>

<style scoped>
.order-page {
    max-width: 600px;
    margin: 0 auto;
    min-height: 100vh;
    background: #faf6f2;
    padding-bottom: 80px;
}

/* 顶部 */
.order-header {
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
.header-title {
    font-size: 17px;
    font-weight: 600;
    color: #1a1a1a;
    margin-left: 8px;
}

/* 核心信息卡片 */
.info-card {
    margin: 16px;
    padding: 24px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.05);
    text-align: center;
}
.card-room {
    margin-bottom: 12px;
}
.card-room-name {
    font-size: 14px;
    color: #888;
}
.card-seat-no {
    font-size: 48px;
    font-weight: 700;
    color: #3A5F73;
    letter-spacing: 4px;
    margin: 8px 0 16px;
}
.card-time {
    font-size: 15px;
    color: #555;
    margin-bottom: 4px;
}
.card-time i {
    margin-right: 4px;
    color: #3A5F73;
}
.card-date {
    font-size: 13px;
    color: #aaa;
    margin-bottom: 16px;
}
.card-change {
    display: inline-block;
    font-size: 13px;
    color: #3A5F73;
    cursor: pointer;
    padding: 6px 16px;
    border-radius: 20px;
    background: #e8f0f5;
    transition: all 0.2s;
}
.card-change:hover {
    background: #d0e2ed;
}

/* 用户信息 */
.user-card {
    margin: 0 16px 16px;
    padding: 20px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.user-field {
    margin-bottom: 16px;
}
.user-field:last-child {
    margin-bottom: 0;
}
.user-field label {
    display: block;
    font-size: 13px;
    color: #888;
    margin-bottom: 6px;
}
.user-field .el-input {
    border-radius: 8px;
}

/* 底部操作栏 */
.bottom-bar {
    position: fixed;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    width: 90%;
    max-width: 560px;
    display: flex;
    gap: 12px;
    z-index: 100;
}
.bar-btn-back {
    flex: 1;
    border-radius: 12px;
    padding: 14px;
    font-size: 15px;
}
.bar-btn-confirm {
    flex: 2;
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
.order-page { animation: fadeUp 0.3s ease; }
</style>

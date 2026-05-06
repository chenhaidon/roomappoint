<template>
    <div v-if="enabled" class="ai-panel-wrap">
        <!-- Floating bubble -->
        <div class="ai-bubble-entrance" v-show="!panelVisible" @click="togglePanel">
            <div class="bubble-pulse"></div>
            <div class="bubble-btn">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2C6.48 2 2 6.04 2 11c0 2.76 1.44 5.22 3.68 6.82L4 22l4.68-2.34C9.76 19.88 10.86 20 12 20c5.52 0 10-4.04 10-9s-4.48-9-10-9z" fill="currentColor"/>
                    <circle cx="8" cy="11" r="1.2" fill="#fff"/>
                    <circle cx="12" cy="11" r="1.2" fill="#fff"/>
                    <circle cx="16" cy="11" r="1.2" fill="#fff"/>
                </svg>
            </div>
            <span class="bubble-label">AI 助手</span>
        </div>

        <!-- Panel -->
        <transition name="panel-slide">
            <div v-show="panelVisible" class="ai-panel" :class="{ 'ai-panel--fullscreen': isFullscreen }">
                <!-- Header -->
                <div class="panel-header">
                    <div class="header-left">
                        <div class="header-dot" :class="streaming ? 'header-dot--active' : ''"></div>
                        <span class="header-title">智能助手</span>
                        <span class="header-badge" v-if="streaming">思考中...</span>
                    </div>
                    <div class="header-actions">
                        <button class="header-btn" @click="clearChat" title="清空">
                            <i class="el-icon-delete"></i>
                        </button>
                        <button class="header-btn" @click="isFullscreen = !isFullscreen" title="全屏">
                            <i :class="isFullscreen ? 'el-icon-copy-document' : 'el-icon-full-screen'"></i>
                        </button>
                        <button class="header-btn" @click="panelVisible = false" title="关闭">
                            <i class="el-icon-close"></i>
                        </button>
                    </div>
                </div>

                <!-- Content area -->
                <div class="panel-body" ref="messageList">
                    <!-- Empty state -->
                    <div v-if="items.length === 0 && !streaming" class="empty-state">
                        <div class="empty-icon">
                            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                                <circle cx="24" cy="24" r="20" stroke="#C0C4CC" stroke-width="2" stroke-dasharray="4 3"/>
                                <path d="M18 20h12M18 26h8" stroke="#C0C4CC" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                        </div>
                        <p class="empty-title">有什么可以帮你的？</p>
                        <p class="empty-desc">可以问我关于自习室、座位预约、积分等问题</p>
                        <div class="empty-suggestions">
                            <button class="suggestion-chip" v-for="s in suggestions" :key="s.text" @click="quickSend(s.text)">
                                <i :class="s.icon"></i>
                                {{ s.label }}
                            </button>
                        </div>
                    </div>

                    <!-- Message items -->
                    <div v-for="(item, idx) in items" :key="idx" class="panel-item">
                        <!-- User message -->
                        <div v-if="item.type === 'user'" class="item-user">
                            <div class="user-bubble">{{ item.content }}</div>
                        </div>

                        <!-- AI text -->
                        <div v-else-if="item.type === 'text'" class="item-ai">
                            <div class="ai-avatar">
                                <i class="el-icon-service"></i>
                            </div>
                            <div class="ai-text" v-html="formatContent(item.content)"></div>
                        </div>

                        <!-- Tool status -->
                        <div v-else-if="item.type === 'tool-status'" class="item-tool-status">
                            <i class="el-icon-loading"></i>
                            <span>{{ item.label }}</span>
                        </div>

                        <!-- Room list card -->
                        <div v-else-if="item.type === 'card-rooms'" class="item-ai">
                            <div class="ai-avatar"><i class="el-icon-service"></i></div>
                            <div class="card-stack">
                                <div class="result-label">自习室列表</div>
                                <div class="room-cards">
                                    <div class="room-card" v-for="(room, ri) in item.data" :key="ri">
                                        <div class="room-card-header">
                                            <span class="room-name">{{ room.Name || room.name }}</span>
                                            <el-tag size="mini" :type="room.Status === 1 || room.StatusFormat === '开放' ? 'success' : 'info'">
                                                {{ room.StatusFormat || (room.Status === 1 ? '开放' : '关闭') }}
                                            </el-tag>
                                        </div>
                                        <div class="room-card-body">
                                            <div class="room-meta" v-if="room.Location || room.location">
                                                <i class="el-icon-location"></i> {{ room.Location || room.location }}
                                            </div>
                                            <div class="room-meta" v-if="room.Description || room.description">
                                                <i class="el-icon-document"></i> {{ (room.Description || room.description || '').substring(0, 40) }}...
                                            </div>
                                        </div>
                                        <div class="room-card-actions">
                                            <button class="card-action-btn" @click="quickSend('查看' + (room.Name || room.name) + '的座位')">查看座位</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Seat grid card -->
                        <div v-else-if="item.type === 'card-seats'" class="item-ai">
                            <div class="ai-avatar"><i class="el-icon-service"></i></div>
                            <div class="card-stack">
                                <div class="result-label">座位状态 · {{ item.roomName || '' }}</div>
                                <div class="seat-stats">
                                    <div class="stat-item stat-item--free">
                                        <span class="stat-num">{{ countSeats(item.data, 'available') }}</span>
                                        <span class="stat-label">空闲</span>
                                    </div>
                                    <div class="stat-item stat-item--used">
                                        <span class="stat-num">{{ countSeats(item.data, 'occupied') }}</span>
                                        <span class="stat-label">已占</span>
                                    </div>
                                    <div class="stat-item stat-item--fix">
                                        <span class="stat-num">{{ countSeats(item.data, 'maintenance') }}</span>
                                        <span class="stat-label">维护</span>
                                    </div>
                                </div>
                                <div class="seat-grid">
                                    <div v-for="(seat, si) in item.data" :key="si"
                                        class="seat-cell"
                                        :class="seatClass(seat)"
                                        @click="onSeatClick(seat, item)">
                                        <span class="seat-no">{{ seat.No || seat.SeatNo || seat.no || ('S' + (si+1)) }}</span>
                                    </div>
                                </div>
                                <div class="seat-legend">
                                    <span class="legend-item"><i class="legend-dot legend-dot--free"></i>空闲</span>
                                    <span class="legend-item"><i class="legend-dot legend-dot--used"></i>已占</span>
                                    <span class="legend-item"><i class="legend-dot legend-dot--fix"></i>维护</span>
                                </div>
                            </div>
                        </div>

                        <!-- Appointment list card -->
                        <div v-else-if="item.type === 'card-appointments'" class="item-ai">
                            <div class="ai-avatar"><i class="el-icon-service"></i></div>
                            <div class="card-stack">
                                <div class="result-label">我的预约</div>
                                <div v-if="item.data.length === 0" class="empty-card">暂无预约记录</div>
                                <div class="appoint-list" v-else>
                                    <div class="appoint-card" v-for="(ap, ai) in item.data" :key="ai">
                                        <div class="appoint-top">
                                            <span class="appoint-room">{{ ap.RoomName || ap.roomName || '自习室' }}</span>
                                            <el-tag size="mini" :type="appointTagType(ap)">{{ formatAppointStatus(ap) }}</el-tag>
                                        </div>
                                        <div class="appoint-info">
                                            <div><i class="el-icon-date"></i> {{ ap.AppointDate || ap.appointDate }}</div>
                                            <div><i class="el-icon-time"></i> {{ ap.BeginTime || ap.beginTime }} - {{ ap.EndTime || ap.endTime }}</div>
                                            <div><i class="el-icon-office-building"></i> 座位 {{ ap.SeatNo || ap.seatNo || ap.No || '-' }}</div>
                                        </div>
                                        <div class="appoint-actions" v-if="canCancel(ap)">
                                            <button class="card-action-btn card-action-btn--danger" @click="quickSend('取消预约 #' + (ap.Id || ap.id))">取消预约</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Points card -->
                        <div v-else-if="item.type === 'card-integral'" class="item-ai">
                            <div class="ai-avatar"><i class="el-icon-service"></i></div>
                            <div class="card-stack">
                                <div class="result-label">积分信息</div>
                                <div class="integral-card">
                                    <div class="integral-main">
                                        <span class="integral-num">{{ item.data.Integral || item.data.integral || 0 }}</span>
                                        <span class="integral-unit">积分</span>
                                    </div>
                                    <div class="integral-tip">每日签到 +2 | 预约评论 +2</div>
                                </div>
                            </div>
                        </div>

                        <!-- Action result card -->
                        <div v-else-if="item.type === 'card-action'" class="item-ai">
                            <div class="ai-avatar"><i class="el-icon-service"></i></div>
                            <div class="card-stack">
                                <div class="action-card" :class="item.success ? 'action-card--success' : 'action-card--fail'">
                                    <i :class="item.success ? 'el-icon-circle-check' : 'el-icon-circle-close'"></i>
                                    <span>{{ item.content }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Streaming text -->
                    <div v-if="streaming && streamingContent && !toolStatus" class="item-ai">
                        <div class="ai-avatar"><i class="el-icon-service"></i></div>
                        <div class="ai-text" v-html="formatContent(streamingContent)"></div>
                        <span class="typing-cursor">|</span>
                    </div>

                    <!-- Tool executing status -->
                    <div v-if="toolStatus" class="item-tool-status">
                        <i class="el-icon-loading"></i>
                        <span>{{ toolStatus }}</span>
                    </div>
                </div>

                <!-- Quick actions -->
                <div class="panel-actions">
                    <button class="quick-btn" @click="quickSend('查看所有自习室')">
                        <i class="el-icon-office-building"></i>
                        <span>查自习室</span>
                    </button>
                    <button class="quick-btn" @click="quickSend('查看空闲座位')">
                        <i class="el-icon-date"></i>
                        <span>查空座</span>
                    </button>
                    <button class="quick-btn" @click="quickSend('查看我的预约')">
                        <i class="el-icon-tickets"></i>
                        <span>我的预约</span>
                    </button>
                    <button class="quick-btn" @click="quickSend('查看我的积分')">
                        <i class="el-icon-coin"></i>
                        <span>查积分</span>
                    </button>
                </div>

                <!-- Input -->
                <div class="panel-input">
                    <el-input v-model="inputText" placeholder="输入问题，如预约明天上午的座位" size="small"
                        @keyup.enter.native="sendMessage" :disabled="streaming" clearable>
                    </el-input>
                    <button class="send-btn" :class="{ 'send-btn--active': inputText.trim() }"
                        @click="sendMessage" :disabled="streaming || !inputText.trim()">
                        <i class="el-icon-s-promotion"></i>
                    </button>
                </div>
            </div>
        </transition>
    </div>
</template>

<script>
export default {
    name: "AiChatBubble",
    data() {
        return {
            enabled: false,
            panelVisible: false,
            isFullscreen: false,
            inputText: "",
            items: [],
            messages: [],
            streaming: false,
            streamingContent: "",
            toolStatus: "",
            conversationId: "",
            suggestions: [
                { text: "查看所有自习室", label: "查自习室", icon: "el-icon-office-building" },
                { text: "查看空闲座位", label: "查空座", icon: "el-icon-date" },
                { text: "查看我的预约", label: "我的预约", icon: "el-icon-tickets" },
                { text: "查看我的积分", label: "查积分", icon: "el-icon-coin" },
            ],
        }
    },
    created() {
        this.conversationId = this.generateId();
        this.checkEnabled();
    },
    methods: {
        generateId() {
            return 'conv_' + Date.now() + '_' + Math.random().toString(36).substring(2, 11);
        },
        async checkEnabled() {
            try {
                const res = await this.$Post("/Chat/CheckEnabled", {});
                if (res && res.Success) {
                    this.enabled = res.Data.Enabled;
                }
            } catch (e) {
                this.enabled = false;
            }
        },
        togglePanel() {
            this.panelVisible = !this.panelVisible;
        },
        clearChat() {
            this.items = [];
            this.messages = [];
            this.conversationId = this.generateId();
        },
        quickSend(text) {
            this.inputText = text;
            this.sendMessage();
        },
        getToolLabel(toolName) {
            const labels = {
                'query_rooms': '正在查询自习室...',
                'query_seats': '正在查询座位...',
                'query_my_appointments': '正在查询预约记录...',
                'query_integral': '正在查询积分...',
                'create_appointment': '正在创建预约...',
                'cancel_appointment': '正在取消预约...',
            };
            return labels[toolName] || '正在处理...';
        },
        formatContent(content) {
            if (!content) return '';
            return content
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/\n/g, '<br>')
                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                .replace(/`(.*?)`/g, '<code>$1</code>');
        },
        scrollToBottom() {
            this.$nextTick(() => {
                const el = this.$refs.messageList;
                if (el) el.scrollTop = el.scrollHeight;
            });
        },
        // Parse tool result into structured data
        parseToolResult(resultText) {
            if (!resultText) return null;
            try {
                const data = JSON.parse(resultText);
                return data;
            } catch (e) {
                // Try to extract JSON from text
                const match = resultText.match(/\[[\s\S]*\]|\{[\s\S]*\}/);
                if (match) {
                    try { return JSON.parse(match[0]); } catch (e2) {}
                }
                return null;
            }
        },
        // Determine card type from tool name
        getCardType(toolName) {
            const map = {
                'query_rooms': 'card-rooms',
                'query_seats': 'card-seats',
                'query_my_appointments': 'card-appointments',
                'query_integral': 'card-integral',
                'create_appointment': 'card-action',
                'cancel_appointment': 'card-action',
            };
            return map[toolName] || null;
        },
        // Seat helpers
        seatClass(seat) {
            const status = seat.Status ?? seat.status ?? seat.SeatStatus ?? 0;
            const occupied = seat.Occupied ?? seat.occupied;
            const maintenance = seat.Maintenance ?? seat.maintenance;
            if (maintenance || status === 3) return 'seat-cell--fix';
            if (occupied || status === 2) return 'seat-cell--used';
            return 'seat-cell--free';
        },
        countSeats(seats, type) {
            if (!seats) return 0;
            return seats.filter(s => {
                const cls = this.seatClass(s);
                if (type === 'available') return cls === 'seat-cell--free';
                if (type === 'occupied') return cls === 'seat-cell--used';
                if (type === 'maintenance') return cls === 'seat-cell--fix';
                return false;
            }).length;
        },
        onSeatClick(seat, item) {
            const status = seat.Status ?? seat.status ?? 0;
            const occupied = seat.Occupied ?? seat.occupied;
            const maintenance = seat.Maintenance ?? seat.maintenance;
            if (maintenance || status === 3) return;
            if (occupied || status === 2) return;
            const seatId = seat.Id || seat.id || seat.SeatId || seat.seatId;
            const roomId = seat.RoomId || seat.roomId || item.roomId;
            const seatNo = seat.No || seat.SeatNo || seat.no || '';
            if (seatId && roomId) {
                this.quickSend(`预约座位 ${seatNo}（ID:${seatId}），自习室ID:${roomId}，今天`);
            }
        },
        // Appointment helpers
        formatAppointStatus(ap) {
            const s = ap.AppointStatus ?? ap.appointStatus ?? ap.Status ?? 0;
            const map = { 0: '待使用', 1: '使用中', 2: '待评论', 3: '已完成', 4: '用户取消', 5: '逾期取消' };
            return ap.AppointStatusFormat || ap.StatusFormat || map[s] || '未知';
        },
        appointTagType(ap) {
            const s = ap.AppointStatus ?? ap.appointStatus ?? ap.Status ?? 0;
            const map = { 0: 'warning', 1: 'success', 2: '', 3: 'info', 4: 'danger', 5: 'danger' };
            return map[s] || 'info';
        },
        canCancel(ap) {
            const s = ap.AppointStatus ?? ap.appointStatus ?? ap.Status ?? 0;
            return s === 0;
        },
        // Process tool result and add card item
        handleToolDone(toolName, resultText) {
            const cardType = this.getCardType(toolName);
            if (!cardType) return;

            const parsed = this.parseToolResult(resultText);
            if (!parsed) return;

            if (cardType === 'card-rooms') {
                const rooms = Array.isArray(parsed) ? parsed : (parsed.Items || parsed.items || parsed.Data || [parsed]);
                if (rooms.length > 0) {
                    this.items.push({ type: 'card-rooms', data: rooms });
                }
            } else if (cardType === 'card-seats') {
                const seats = Array.isArray(parsed) ? parsed : (parsed.Items || parsed.items || parsed.Data || [parsed]);
                if (seats.length > 0) {
                    this.items.push({ type: 'card-seats', data: seats, roomName: '', roomId: '' });
                }
            } else if (cardType === 'card-appointments') {
                const appts = Array.isArray(parsed) ? parsed : (parsed.Items || parsed.items || parsed.Data || [parsed]);
                if (appts.length > 0) {
                    this.items.push({ type: 'card-appointments', data: appts });
                }
            } else if (cardType === 'card-integral') {
                this.items.push({ type: 'card-integral', data: parsed });
            } else if (cardType === 'card-action') {
                const success = resultText.includes('成功') || resultText.includes('success');
                this.items.push({ type: 'card-action', content: resultText.length < 100 ? resultText : (success ? '操作成功' : '操作失败'), success });
            }
        },
        async sendMessage() {
            const text = this.inputText.trim();
            if (!text || this.streaming) return;

            this.items.push({ type: 'user', content: text });
            this.messages.push({ role: 'user', content: text });
            this.inputText = "";
            this.streaming = true;
            this.streamingContent = "";
            this.scrollToBottom();

            try {
                const token = this.$store.getters.Token;
                const response = await fetch('/api/Chat/Send', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': token
                    },
                    body: JSON.stringify({
                        Message: text,
                        ConversationId: this.conversationId
                    })
                });

                if (!response.ok) {
                    const errText = await response.text();
                    let errMsg = '请求失败';
                    try {
                        const errJson = JSON.parse(errText);
                        errMsg = errJson.Msg || errJson.Data || errMsg;
                    } catch (e) { errMsg = errText || errMsg; }
                    this.items.push({ type: 'text', content: '抱歉，出错了：' + errMsg });
                    this.streaming = false;
                    this.scrollToBottom();
                    return;
                }

                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let buffer = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    buffer += decoder.decode(value, { stream: true });
                    const lines = buffer.split('\n');
                    buffer = lines.pop();

                    for (const line of lines) {
                        if (!line.startsWith('data:')) continue;
                        const data = line.substring(5).trim();
                        if (data === '[DONE]') continue;
                        try {
                            const parsed = JSON.parse(data);
                            if (parsed.error) {
                                this.streamingContent += '\n错误：' + parsed.error;
                                this.scrollToBottom();
                                continue;
                            }
                            // Tool executing
                            if (parsed.tool && parsed.status === 'executing') {
                                this.toolStatus = this.getToolLabel(parsed.tool);
                                this.items.push({ type: 'tool-status', label: this.toolStatus, tool: parsed.tool });
                                this.scrollToBottom();
                                continue;
                            }
                            // Tool done — render as card
                            if (parsed.tool && parsed.status === 'done') {
                                this.toolStatus = '';
                                // Remove the last tool-status item
                                for (let i = this.items.length - 1; i >= 0; i--) {
                                    if (this.items[i].type === 'tool-status') { this.items.splice(i, 1); break; }
                                }
                                this.handleToolDone(parsed.tool, parsed.result);
                                this.scrollToBottom();
                                continue;
                            }
                            // Text content
                            const token = parsed.content || '';
                            if (token) {
                                this.streamingContent += token;
                                this.scrollToBottom();
                            }
                        } catch (e) { /* skip */ }
                    }
                }

                if (this.streamingContent) {
                    this.items.push({ type: 'text', content: this.streamingContent });
                    this.messages.push({ role: 'assistant', content: this.streamingContent });
                }

            } catch (e) {
                this.items.push({ type: 'text', content: '网络错误，请稍后重试。' });
            } finally {
                this.streaming = false;
                this.streamingContent = "";
                this.scrollToBottom();
            }
        }
    }
}
</script>

<style scoped>
/* ===== Variables ===== */
.ai-panel-wrap {
    --ap-primary: #2E4A62;
    --ap-primary-light: #3a6b8c;
    --ap-accent: #E6B980;
    --ap-bg: #F2F3F5;
    --ap-surface: #FFFFFF;
    --ap-border: #E5E7EB;
    --ap-text: #1F2937;
    --ap-text-secondary: #6B7280;
    --ap-text-muted: #9CA3AF;
    --ap-success: #10B981;
    --ap-warning: #F59E0B;
    --ap-danger: #EF4444;
    --ap-radius: 12px;
    --ap-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
    --ap-shadow-md: 0 4px 12px rgba(0,0,0,0.08), 0 2px 4px rgba(0,0,0,0.04);
    --ap-shadow-lg: 0 10px 40px rgba(0,0,0,0.12);
    --ap-transition: 200ms cubic-bezier(0.4, 0, 0.2, 1);

    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 9999;
}

/* ===== Floating bubble ===== */
.ai-bubble-entrance {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    position: relative;
}

.bubble-btn {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--ap-primary), var(--ap-primary-light));
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: var(--ap-shadow-lg);
    transition: transform var(--ap-transition);
    position: relative;
    z-index: 1;
}

.bubble-pulse {
    position: absolute;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--ap-primary), var(--ap-primary-light));
    animation: bubblePulse 2.5s ease-in-out infinite;
    z-index: 0;
}

@keyframes bubblePulse {
    0%, 100% { transform: scale(1); opacity: 0.4; }
    50% { transform: scale(1.4); opacity: 0; }
}

.ai-bubble-entrance:hover .bubble-btn {
    transform: scale(1.08);
}

.bubble-label {
    font-size: 12px;
    font-weight: 600;
    color: var(--ap-primary);
    margin-top: 6px;
    letter-spacing: 0.5px;
}

/* ===== Panel ===== */
.ai-panel {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 400px;
    height: 600px;
    background: var(--ap-surface);
    border-radius: var(--ap-radius);
    box-shadow: var(--ap-shadow-lg);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border: 1px solid var(--ap-border);
}

.ai-panel--fullscreen {
    position: fixed;
    top: 20px;
    left: 20px;
    right: 20px;
    bottom: 20px;
    width: auto;
    height: auto;
    border-radius: 16px;
}

.panel-slide-enter-active,
.panel-slide-leave-active {
    transition: all 300ms cubic-bezier(0.34, 1.56, 0.64, 1);
}

.panel-slide-enter,
.panel-slide-leave-to {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
}

/* ===== Header ===== */
.panel-header {
    height: 52px;
    background: linear-gradient(135deg, var(--ap-primary), var(--ap-primary-light));
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    flex-shrink: 0;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 8px;
}

.header-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--ap-success);
}

.header-dot--active {
    animation: dotBlink 1s infinite;
}

@keyframes dotBlink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.3; }
}

.header-title {
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 0.5px;
}

.header-badge {
    font-size: 11px;
    color: rgba(255,255,255,0.7);
    background: rgba(255,255,255,0.15);
    padding: 2px 8px;
    border-radius: 10px;
}

.header-actions {
    display: flex;
    gap: 2px;
}

.header-btn {
    background: none;
    border: none;
    color: rgba(255,255,255,0.7);
    font-size: 16px;
    cursor: pointer;
    padding: 6px;
    border-radius: 6px;
    transition: all var(--ap-transition);
    display: flex;
    align-items: center;
    justify-content: center;
}

.header-btn:hover {
    color: #fff;
    background: rgba(255,255,255,0.15);
}

/* ===== Body ===== */
.panel-body {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background: var(--ap-bg);
}

.panel-body::-webkit-scrollbar {
    width: 4px;
}

.panel-body::-webkit-scrollbar-thumb {
    background: #D1D5DB;
    border-radius: 2px;
}

/* ===== Empty state ===== */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    text-align: center;
    padding: 20px;
}

.empty-icon {
    margin-bottom: 16px;
    opacity: 0.5;
}

.empty-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--ap-text);
    margin: 0 0 6px;
}

.empty-desc {
    font-size: 13px;
    color: var(--ap-text-muted);
    margin: 0 0 20px;
}

.empty-suggestions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
}

.suggestion-chip {
    font-size: 12px;
    color: var(--ap-primary);
    background: var(--ap-surface);
    border: 1px solid var(--ap-border);
    padding: 6px 14px;
    border-radius: 20px;
    cursor: pointer;
    transition: all var(--ap-transition);
    display: flex;
    align-items: center;
    gap: 4px;
}

.suggestion-chip:hover {
    background: var(--ap-primary);
    color: #fff;
    border-color: var(--ap-primary);
}

/* ===== Items ===== */
.panel-item {
    margin-bottom: 12px;
}

/* User message */
.item-user {
    display: flex;
    justify-content: flex-end;
}

.user-bubble {
    max-width: 80%;
    background: var(--ap-primary);
    color: #fff;
    padding: 8px 14px;
    border-radius: 12px 12px 4px 12px;
    font-size: 13px;
    line-height: 1.5;
    word-break: break-word;
}

/* AI message */
.item-ai {
    display: flex;
    gap: 8px;
    align-items: flex-start;
}

.ai-avatar {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: var(--ap-primary);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    flex-shrink: 0;
}

.ai-text {
    background: var(--ap-surface);
    padding: 10px 14px;
    border-radius: 4px 12px 12px 12px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--ap-text);
    box-shadow: var(--ap-shadow);
    max-width: 85%;
    word-break: break-word;
}

.ai-text >>> code {
    background: #F3F4F6;
    padding: 1px 4px;
    border-radius: 4px;
    font-size: 12px;
    color: var(--ap-primary);
}

.ai-text >>> strong {
    font-weight: 600;
}

.typing-cursor {
    animation: blink 1s infinite;
    color: var(--ap-text-muted);
    font-size: 14px;
    margin-left: 2px;
}

@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

/* Tool status */
.item-tool-status {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: var(--ap-text-muted);
    padding: 4px 0;
}

.item-tool-status i {
    font-size: 12px;
}

/* ===== Cards ===== */
.card-stack {
    flex: 1;
    min-width: 0;
}

.result-label {
    font-size: 11px;
    font-weight: 600;
    color: var(--ap-text-muted);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 8px;
}

/* Room cards */
.room-cards {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.room-card {
    background: var(--ap-surface);
    border-radius: var(--ap-radius);
    padding: 12px;
    box-shadow: var(--ap-shadow);
    border: 1px solid var(--ap-border);
    transition: all var(--ap-transition);
}

.room-card:hover {
    box-shadow: var(--ap-shadow-md);
    transform: translateY(-1px);
}

.room-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.room-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--ap-text);
}

.room-card-body {
    margin-bottom: 8px;
}

.room-meta {
    font-size: 12px;
    color: var(--ap-text-secondary);
    margin-bottom: 2px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.room-meta i {
    font-size: 12px;
    color: var(--ap-text-muted);
}

.room-card-actions {
    display: flex;
    justify-content: flex-end;
}

.card-action-btn {
    font-size: 12px;
    color: var(--ap-primary);
    background: rgba(46, 74, 98, 0.06);
    border: none;
    padding: 5px 14px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
    transition: all var(--ap-transition);
}

.card-action-btn:hover {
    background: rgba(46, 74, 98, 0.12);
}

.card-action-btn--danger {
    color: var(--ap-danger);
    background: rgba(239, 68, 68, 0.06);
}

.card-action-btn--danger:hover {
    background: rgba(239, 68, 68, 0.12);
}

/* Seat grid */
.seat-stats {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
}

.stat-item {
    flex: 1;
    text-align: center;
    padding: 8px;
    border-radius: 8px;
    background: var(--ap-surface);
    box-shadow: var(--ap-shadow);
}

.stat-num {
    display: block;
    font-size: 20px;
    font-weight: 700;
    line-height: 1.2;
}

.stat-label {
    font-size: 11px;
    color: var(--ap-text-muted);
}

.stat-item--free .stat-num { color: var(--ap-success); }
.stat-item--used .stat-num { color: var(--ap-danger); }
.stat-item--fix .stat-num { color: var(--ap-text-muted); }

.seat-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(44px, 1fr));
    gap: 6px;
    margin-bottom: 10px;
}

.seat-cell {
    aspect-ratio: 1;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 600;
    cursor: pointer;
    transition: all var(--ap-transition);
    border: 2px solid transparent;
}

.seat-no {
    pointer-events: none;
}

.seat-cell--free {
    background: #ECFDF5;
    color: #059669;
    border-color: #A7F3D0;
}

.seat-cell--free:hover {
    background: #D1FAE5;
    transform: scale(1.08);
}

.seat-cell--used {
    background: #FEF2F2;
    color: #DC2626;
    border-color: #FECACA;
    cursor: default;
}

.seat-cell--fix {
    background: #F3F4F6;
    color: #9CA3AF;
    border-color: #E5E7EB;
    cursor: default;
}

.seat-legend {
    display: flex;
    gap: 12px;
    justify-content: center;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: var(--ap-text-muted);
}

.legend-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    display: inline-block;
}

.legend-dot--free { background: var(--ap-success); }
.legend-dot--used { background: var(--ap-danger); }
.legend-dot--fix { background: #D1D5DB; }

/* Appointment cards */
.appoint-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.appoint-card {
    background: var(--ap-surface);
    border-radius: var(--ap-radius);
    padding: 12px;
    box-shadow: var(--ap-shadow);
    border: 1px solid var(--ap-border);
}

.appoint-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.appoint-room {
    font-size: 14px;
    font-weight: 600;
    color: var(--ap-text);
}

.appoint-info {
    font-size: 12px;
    color: var(--ap-text-secondary);
    line-height: 1.8;
}

.appoint-info i {
    margin-right: 4px;
    color: var(--ap-text-muted);
}

.appoint-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid var(--ap-border);
}

/* Points card */
.integral-card {
    background: linear-gradient(135deg, var(--ap-primary), var(--ap-primary-light));
    border-radius: var(--ap-radius);
    padding: 20px;
    text-align: center;
    color: #fff;
}

.integral-main {
    margin-bottom: 6px;
}

.integral-num {
    font-size: 36px;
    font-weight: 700;
    letter-spacing: -1px;
}

.integral-unit {
    font-size: 14px;
    opacity: 0.8;
    margin-left: 4px;
}

.integral-tip {
    font-size: 11px;
    opacity: 0.6;
}

/* Action result card */
.action-card {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-radius: var(--ap-radius);
    font-size: 13px;
    font-weight: 500;
}

.action-card i {
    font-size: 20px;
}

.action-card--success {
    background: #ECFDF5;
    color: #059669;
    border: 1px solid #A7F3D0;
}

.action-card--fail {
    background: #FEF2F2;
    color: #DC2626;
    border: 1px solid #FECACA;
}

.empty-card {
    text-align: center;
    padding: 20px;
    color: var(--ap-text-muted);
    font-size: 13px;
    background: var(--ap-surface);
    border-radius: var(--ap-radius);
}

/* ===== Quick actions ===== */
.panel-actions {
    display: flex;
    gap: 6px;
    padding: 10px 16px;
    background: var(--ap-surface);
    border-top: 1px solid var(--ap-border);
    flex-shrink: 0;
    overflow-x: auto;
}

.panel-actions::-webkit-scrollbar {
    display: none;
}

.quick-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: var(--ap-primary);
    background: var(--ap-bg);
    border: 1px solid var(--ap-border);
    padding: 6px 12px;
    border-radius: 20px;
    cursor: pointer;
    white-space: nowrap;
    transition: all var(--ap-transition);
    flex-shrink: 0;
}

.quick-btn i {
    font-size: 13px;
}

.quick-btn:hover {
    background: var(--ap-primary);
    color: #fff;
    border-color: var(--ap-primary);
}

/* ===== Input ===== */
.panel-input {
    display: flex;
    gap: 8px;
    padding: 12px 16px;
    background: var(--ap-surface);
    border-top: 1px solid var(--ap-border);
    flex-shrink: 0;
    align-items: center;
}

.panel-input .el-input {
    flex: 1;
}

.panel-input /deep/ .el-input__inner {
    border-radius: 20px;
    background: var(--ap-bg);
    border-color: var(--ap-border);
    font-size: 13px;
    height: 36px;
    line-height: 36px;
}

.panel-input /deep/ .el-input__inner:focus {
    border-color: var(--ap-primary);
}

.send-btn {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: var(--ap-bg);
    border: 1px solid var(--ap-border);
    color: var(--ap-text-muted);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    transition: all var(--ap-transition);
    flex-shrink: 0;
}

.send-btn--active {
    background: var(--ap-primary);
    border-color: var(--ap-primary);
    color: #fff;
}

.send-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* ===== Mobile ===== */
@media (max-width: 768px) {
    .ai-panel-wrap {
        bottom: 16px;
        right: 16px;
    }

    .ai-panel {
        width: calc(100vw - 32px);
        height: 75vh;
        max-height: 600px;
    }

    .ai-panel--fullscreen {
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        border-radius: 0;
    }

    .bubble-btn {
        width: 48px;
        height: 48px;
    }

    .bubble-pulse {
        width: 48px;
        height: 48px;
    }

    .bubble-label {
        font-size: 11px;
    }
}
</style>

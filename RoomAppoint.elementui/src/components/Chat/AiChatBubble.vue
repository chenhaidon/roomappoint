<template>
    <div v-if="enabled" class="ai-chat-wrap">
        <div class="ai-chat-bubble-wrap" v-show="!panelVisible" @click="togglePanel">
            <div class="ai-chat-label">AI助手</div>
            <div class="ai-chat-bubble">
                <i class="el-icon-chat-dot-round"></i>
            </div>
        </div>

        <transition name="el-zoom-in-bottom">
            <div v-show="panelVisible" class="ai-chat-panel">
                <div class="ai-chat-header">
                    <span>智能助手</span>
                    <div class="header-actions">
                        <el-button type="text" icon="el-icon-delete" class="header-btn" @click="clearChat"
                            title="清空对话"></el-button>
                        <el-button type="text" icon="el-icon-close" class="header-btn" @click="panelVisible = false"
                            title="关闭"></el-button>
                    </div>
                </div>

                <div class="ai-chat-messages" ref="messageList">
                    <div class="ai-welcome">
                        <div class="welcome-avatar">
                            <i class="el-icon-service"></i>
                        </div>
                        <div class="welcome-text">你好！我是自习室智能助手，有什么可以帮你的？</div>
                    </div>

                    <div v-for="(msg, idx) in messages" :key="idx" class="ai-msg"
                        :class="msg.role === 'user' ? 'ai-msg-user' : 'ai-msg-assistant'">
                        <div class="msg-avatar" v-if="msg.role === 'assistant'">
                            <i class="el-icon-service"></i>
                        </div>
                        <div class="msg-bubble">
                            <div class="msg-content" v-html="formatContent(msg.content)"></div>
                        </div>
                        <div class="msg-avatar" v-if="msg.role === 'user'">
                            <i class="el-icon-user"></i>
                        </div>
                    </div>

                    <div v-if="toolStatus" class="ai-msg ai-msg-assistant">
                        <div class="msg-avatar">
                            <i class="el-icon-service"></i>
                        </div>
                        <div class="msg-bubble">
                            <div class="msg-content tool-status">
                                <i class="el-icon-loading"></i> {{ toolStatus }}
                            </div>
                        </div>
                    </div>

                    <div v-if="streaming && !toolStatus" class="ai-msg ai-msg-assistant">
                        <div class="msg-avatar">
                            <i class="el-icon-service"></i>
                        </div>
                        <div class="msg-bubble">
                            <div class="msg-content" v-html="formatContent(streamingContent)"></div>
                            <span class="typing-cursor">|</span>
                        </div>
                    </div>
                </div>

                <div class="ai-chat-input">
                    <el-input v-model="inputText" placeholder="输入你的问题..." size="small"
                        @keyup.enter.native="sendMessage" :disabled="streaming">
                    </el-input>
                    <el-button type="primary" size="small" icon="el-icon-s-promotion"
                        :loading="streaming" @click="sendMessage">
                    </el-button>
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
            inputText: "",
            messages: [],
            streaming: false,
            streamingContent: "",
            toolStatus: "",
            conversationId: "",
        }
    },
    created() {
        this.conversationId = this.generateId();
        this.checkEnabled();
    },
    methods: {
        generateId() {
            return 'conv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
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
            this.messages = [];
            this.conversationId = this.generateId();
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
                if (el) {
                    el.scrollTop = el.scrollHeight;
                }
            });
        },
        async sendMessage() {
            const text = this.inputText.trim();
            if (!text || this.streaming) return;

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
                    } catch (e) {
                        errMsg = errText || errMsg;
                    }
                    this.messages.push({ role: 'assistant', content: '抱歉，出错了：' + errMsg });
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
                        if (line.startsWith('data:')) {
                            const data = line.substring(5).trim();
                            if (data === '[DONE]') continue;
                            try {
                                const parsed = JSON.parse(data);
                                if (parsed.error) {
                                    this.streamingContent += '\n错误：' + parsed.error;
                                    this.scrollToBottom();
                                    continue;
                                }
                                // Tool execution notification
                                if (parsed.tool && parsed.status === 'executing') {
                                    this.toolStatus = this.getToolLabel(parsed.tool);
                                    this.scrollToBottom();
                                    continue;
                                }
                                if (parsed.tool && parsed.status === 'done') {
                                    this.toolStatus = '';
                                    continue;
                                }
                                // Text content
                                const token = parsed.content || '';
                                if (token) {
                                    this.streamingContent += token;
                                    this.scrollToBottom();
                                }
                            } catch (e) {
                                // skip unparseable lines
                            }
                        }
                    }
                }

                if (this.streamingContent) {
                    this.messages.push({ role: 'assistant', content: this.streamingContent });
                }

            } catch (e) {
                this.messages.push({ role: 'assistant', content: '网络错误，请稍后重试。' });
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
.ai-chat-wrap {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 9999;
}

.ai-chat-bubble-wrap {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    animation: float 3s ease-in-out infinite;
}

.ai-chat-label {
    background: linear-gradient(135deg, #2E4A62, #3a6b8c);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    padding: 4px 12px;
    border-radius: 12px;
    margin-bottom: 8px;
    white-space: nowrap;
    box-shadow: 0 2px 8px rgba(46, 74, 98, 0.3);
    letter-spacing: 1px;
}

.ai-chat-bubble {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, #2E4A62, #3a6b8c);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
    transition: transform .2s;
    font-size: 26px;
    position: relative;
}

.ai-chat-bubble::after {
    content: '';
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: linear-gradient(135deg, #2E4A62, #3a6b8c);
    animation: pulse 2s ease-in-out infinite;
    z-index: -1;
}

.ai-chat-bubble-wrap:hover .ai-chat-bubble {
    transform: scale(1.1);
}

@keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-6px); }
}

@keyframes pulse {
    0% { transform: scale(1); opacity: 0.6; }
    100% { transform: scale(1.5); opacity: 0; }
}

.ai-chat-panel {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 380px;
    height: 520px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border: 1px solid #e4e7ed;
}

.ai-chat-header {
    height: 48px;
    background: linear-gradient(135deg, #2E4A62, #3a6b8c);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    font-weight: 600;
    font-size: 15px;
    flex-shrink: 0;
}

.header-actions {
    display: flex;
    gap: 4px;
}

.header-btn {
    color: rgba(255, 255, 255, 0.8) !important;
    font-size: 16px;
    padding: 4px !important;
}

.header-btn:hover {
    color: #fff !important;
}

.ai-chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background: #f5f7fa;
}

.ai-welcome {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
}

.welcome-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #2E4A62;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    flex-shrink: 0;
}

.welcome-text {
    background: #fff;
    padding: 8px 12px;
    border-radius: 0 12px 12px 12px;
    font-size: 13px;
    color: #303133;
    line-height: 1.5;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.ai-msg {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
    align-items: flex-start;
}

.ai-msg-user {
    flex-direction: row-reverse;
}

.msg-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    flex-shrink: 0;
}

.ai-msg-assistant .msg-avatar {
    background: #2E4A62;
    color: #fff;
}

.ai-msg-user .msg-avatar {
    background: #409EFF;
    color: #fff;
}

.msg-bubble {
    max-width: 75%;
    padding: 8px 12px;
    border-radius: 12px;
    font-size: 13px;
    line-height: 1.5;
    word-break: break-word;
}

.ai-msg-assistant .msg-bubble {
    background: #fff;
    color: #303133;
    border-radius: 0 12px 12px 12px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.ai-msg-user .msg-bubble {
    background: #2E4A62;
    color: #fff;
    border-radius: 12px 0 12px 12px;
}

.msg-content >>> code {
    background: #f0f2f5;
    padding: 1px 4px;
    border-radius: 3px;
    font-size: 12px;
}

.tool-status {
    color: #909399;
    font-size: 12px;
}

.typing-cursor {
    animation: blink 1s infinite;
    color: #909399;
}

@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

.ai-chat-input {
    display: flex;
    gap: 8px;
    padding: 12px;
    border-top: 1px solid #e4e7ed;
    background: #fff;
    flex-shrink: 0;
}

.ai-chat-input .el-input {
    flex: 1;
}

@media (max-width: 768px) {
    .ai-chat-wrap {
        bottom: 16px;
        right: 16px;
    }

    .ai-chat-panel {
        width: calc(100vw - 32px);
        height: 70vh;
        max-height: 520px;
    }

    .ai-chat-bubble {
        width: 48px;
        height: 48px;
        font-size: 22px;
    }

    .ai-chat-label {
        font-size: 11px;
        padding: 3px 10px;
    }
}
</style>

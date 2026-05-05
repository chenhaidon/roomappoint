<template>
    <div class="app-container">
        <el-card class="box-card">
            <div slot="header" class="clearfix" style="display:flex;align-items:center;justify-content:space-between;">
                <span>AI 助手配置</span>
                <div>
                    <el-button size="mini" type="success" icon="el-icon-connection" :loading="testing" @click="testConnection">
                        测试连通性
                    </el-button>
                    <el-button size="mini" type="primary" icon="el-icon-check" :loading="saving" @click="save">
                        保存配置
                    </el-button>
                </div>
            </div>

            <el-form label-width="120px" style="max-width: 600px;">
                <el-form-item label="启用AI助手">
                    <el-switch v-model="form['ai.enabled']" active-value="1" inactive-value="0"
                        active-text="开启" inactive-text="关闭">
                    </el-switch>
                </el-form-item>

                <el-form-item label="接口地址">
                    <el-input v-model="form['ai.api_url']" placeholder="https://api.anthropic.com">
                    </el-input>
                    <div class="form-tip">Anthropic API 地址或中转站地址，系统会自动拼接 /v1/messages</div>
                </el-form-item>

                <el-form-item label="API密钥">
                    <el-input v-model="form['ai.api_key']" placeholder="sk-ant-xxx" show-password></el-input>
                </el-form-item>

                <el-form-item label="模型名称">
                    <el-input v-model="form['ai.model']" placeholder="claude-sonnet-4-6-20250514"></el-input>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script>
export default {
    name: "AiConfig",
    data() {
        return {
            form: {
                'ai.enabled': '0',
                'ai.api_url': 'https://api.openai.com/v1/chat/completions',
                'ai.api_key': '',
                'ai.model': 'gpt-3.5-turbo',
            },
            saving: false,
            testing: false,
        }
    },
    created() {
        this.loadConfig();
    },
    methods: {
        async loadConfig() {
            const res = await this.$Post("/SystemConfig/List", {});
            if (res && res.Success && res.Data) {
                for (const item of res.Data) {
                    if (this.form.hasOwnProperty(item.ConfigKey)) {
                        this.form[item.ConfigKey] = item.ConfigValue || '';
                    }
                }
            }
        },
        async save() {
            this.saving = true;
            try {
                const configs = Object.keys(this.form).map(key => ({
                    ConfigKey: key,
                    ConfigValue: this.form[key],
                }));
                const res = await this.$Post("/SystemConfig/Save", configs);
                if (res && res.Success) {
                    this.$message.success("保存成功");
                } else {
                    this.$message.error(res.Msg || "保存失败");
                }
            } finally {
                this.saving = false;
            }
        },
        async testConnection() {
            // 先保存最新配置
            await this.save();
            this.testing = true;
            try {
                const res = await this.$Post("/Chat/TestConnection", {});
                if (res && res.Data && res.Data.Success) {
                    this.$message.success(res.Data.Msg || "连接成功");
                } else {
                    this.$message.error((res.Data && res.Data.Msg) || res.Msg || "连接失败");
                }
            } catch (e) {
                this.$message.error("测试请求异常");
            } finally {
                this.testing = false;
            }
        }
    }
}
</script>

<style scoped>
.form-tip {
    color: #909399;
    font-size: 12px;
    line-height: 1.5;
    margin-top: 4px;
}
</style>

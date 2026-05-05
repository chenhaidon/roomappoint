<template>
    <div class="slider-verify" :class="{ 'is-success': verified }">
        <div class="slider-track" ref="track">
            <div class="slider-fill" :style="{ width: moveX + 'px' }"></div>
            <span class="slider-text" v-if="!verified">{{ tips }}</span>
            <span class="slider-text slider-text--success" v-else>
                <i class="el-icon-check"></i> 验证通过
            </span>
            <div class="slider-thumb" ref="thumb"
                @mousedown="onStart"
                @touchstart.prevent="onStart"
                :style="{ left: moveX + 'px' }">
                <i :class="verified ? 'el-icon-check' : 'el-icon-d-arrow-right'"></i>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'SliderCode',
    data() {
        return {
            tips: '请拖动滑块完成验证',
            moveX: 0,
            verified: false,
            startX: 0,
            maxMove: 0,
        }
    },
    mounted() {
        this.maxMove = this.$refs.track.offsetWidth - 40;
        this._onMove = this.onMove.bind(this);
        this._onEnd = this.onEnd.bind(this);
    },
    beforeDestroy() {
        document.removeEventListener('mousemove', this._onMove);
        document.removeEventListener('mouseup', this._onEnd);
        document.removeEventListener('touchmove', this._onMove);
        document.removeEventListener('touchend', this._onEnd);
    },
    methods: {
        onStart(e) {
            if (this.verified) return;
            const clientX = e.touches ? e.touches[0].clientX : e.clientX;
            this.startX = clientX - this.moveX;
            document.addEventListener('mousemove', this._onMove);
            document.addEventListener('mouseup', this._onEnd);
            document.addEventListener('touchmove', this._onMove);
            document.addEventListener('touchend', this._onEnd);
        },
        onMove(e) {
            const clientX = e.touches ? e.touches[0].clientX : e.clientX;
            let x = clientX - this.startX;
            if (x < 0) x = 0;
            if (x > this.maxMove) x = this.maxMove;
            this.moveX = x;
        },
        onEnd() {
            document.removeEventListener('mousemove', this._onMove);
            document.removeEventListener('mouseup', this._onEnd);
            document.removeEventListener('touchmove', this._onMove);
            document.removeEventListener('touchend', this._onEnd);
            if (this.moveX >= this.maxMove - 2) {
                this.verified = true;
                this.moveX = this.maxMove;
                this.$emit('verified');
            } else {
                this.moveX = 0;
            }
        },
        refreshCode() {
            this.moveX = 0;
            this.verified = false;
        },
        getCode() {
            return this.verified ? 'slider-pass' : '';
        }
    }
}
</script>

<style scoped>
.slider-verify {
    width: 100%;
    user-select: none;
}

.slider-track {
    position: relative;
    height: 48px;
    background: #FAF8F3;
    border: 1px solid #E8E3D9;
    border-radius: 12px;
    overflow: hidden;
    transition: all 200ms cubic-bezier(0.22, 1, 0.36, 1);
}

.slider-fill {
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    background: linear-gradient(90deg, rgba(58, 95, 115, 0.08), rgba(58, 95, 115, 0.15));
    transition: none;
}

.slider-text {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    color: #A0AAB4;
    letter-spacing: 2px;
    pointer-events: none;
}

.slider-text--success {
    color: #7A9E82;
    font-weight: 600;
}

.slider-thumb {
    position: absolute;
    top: 3px;
    left: 0;
    width: 42px;
    height: 42px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 200ms cubic-bezier(0.22, 1, 0.36, 1);
    z-index: 2;
}

.slider-thumb:hover {
    box-shadow: 0 4px 12px rgba(58, 95, 115, 0.25);
}

.slider-thumb i {
    color: #3A5F73;
    font-size: 16px;
    font-weight: 700;
}

.is-success .slider-thumb {
    background: #7A9E82;
    cursor: default;
}

.is-success .slider-thumb i {
    color: #fff;
}

.is-success .slider-track {
    border-color: #7A9E82;
    background: rgba(122, 158, 130, 0.08);
}
</style>

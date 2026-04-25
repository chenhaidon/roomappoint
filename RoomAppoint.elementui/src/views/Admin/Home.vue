<template>
    <div>
        <el-card class="box-card margin-top-lg">
            <div slot="header" class="clearfix">
                <span>数据统计</span>

            </div>
            <div class="board-list " v-if="DataCollect != null">
                <div class="board-item">
                    <div class="tit">自习室个数</div>
                    <div class="num">{{ DataCollect.RoomCount }}</div>
                </div>
                <div class="board-item">
                    <div class="tit">总座位数</div>
                    <div class="num">{{ DataCollect.SeatCount }}</div>
                </div>
                <div class="board-item">
                    <div class="tit">总预约人次</div>
                    <div class="num">{{ DataCollect.AppointCount }}</div>
                </div>
                <div class="board-item">
                    <div class="tit">待使用次数</div>
                    <div class="num">{{ DataCollect.WaitAppointCount }}</div>
                </div>
                <div class="board-item">
                    <div class="tit">逾期人次</div>
                    <div class="num">{{ DataCollect.OverdueAppointCount }}</div>
                </div>


                <div class="board-item">
                    <div class="tit">总剩余积分</div>
                    <div class="num">{{ DataCollect.TotalIntegral }}</div>
                </div>
            </div>
        </el-card>


        <el-card class="box-card margin-top-lg">
            <div slot="header" class="clearfix">
                <span>今日每个自习室早中晚的使用率(%)</span>

            </div>
            <div class="echart" id="echartDiv" :style="{ float: 'left', width: '100%', height: '500px' }">
            </div>
        </el-card>

        <el-card class="box-card margin-top-lg">
            <div slot="header" class="clearfix">
                <span>欢迎您的使用</span>

            </div>
            <el-row :gutter="10">
                <el-col :span="12">
                    <el-calendar></el-calendar>
                </el-col>
                <el-col :span="12">
                    <el-descriptions title="系统介绍" direction="horizontal" :column="1" border>
                        <el-descriptions-item label="系统名称">基于SpringBoot+Vue的自习室管理系统设计与实现</el-descriptions-item>
                        <el-descriptions-item label="后端技术">SpringBoot3.3</el-descriptions-item>
                        <el-descriptions-item label="前端">Vue2</el-descriptions-item>
                        <el-descriptions-item label="数据库">Mysql</el-descriptions-item>
                        <!-- <el-descriptions-item label="资料/文档/源码获取方式">
                            <el-tag size="small"><a
                                    href="https://www.bilibili.com/video/BV12JYyeCEeF/?spm_id_from=333.337.search-card.all.click&vd_source=c2f2894f61f784c10e213af8351b39fd">点击跳转字母哥B站（私信获取）</a></el-tag>
                        </el-descriptions-item> -->
                        <!-- <el-descriptions-item label="字母哥唯一微信">zimuge404</el-descriptions-item>
                        <el-descriptions-item label="系统是否收费">资料/文档/源码/数据库 <span style="color:red">全部免费(同行勿扰)</span>
                        </el-descriptions-item> -->
                    </el-descriptions>
                </el-col>
            </el-row>

        </el-card>



    </div>
</template>

<script>
import * as echarts from "echarts";
export default {
    name: 'Home',
    props: {

    },
    data() {
        return {
            DataCollect: null
        };
    },
    created() {

    },
    mounted() {
        this.GetAppointRoomUseRate();
        this.GetDataCollect();

    },
    methods: {
        //统计早中晚每个自习室的使用率&空闲率
        async GetAppointRoomUseRate() {
            let { Data } = await this.$Post('/AppointRecord/GetAppointRoomUseRate', {});

            let myChart = echarts.init(document.getElementById("echartDiv"));// 图标初始化
            const posList = [
                'left',
                'right',
                'top',
                'bottom',
                'inside',
                'insideTop',
                'insideLeft',
                'insideRight',
                'insideBottom',
                'insideTopLeft',
                'insideTopRight',
                'insideBottomLeft',
                'insideBottomRight'
            ];
            app.configParameters = {
                rotate: {
                    min: -90,
                    max: 90
                },
                align: {
                    options: {
                        left: 'left',
                        center: 'center',
                        right: 'right'
                    }
                },
                verticalAlign: {
                    options: {
                        top: 'top',
                        middle: 'middle',
                        bottom: 'bottom'
                    }
                },
                position: {
                    options: posList.reduce(function (map, pos) {
                        map[pos] = pos;
                        return map;
                    }, {})
                },
                distance: {
                    min: 0,
                    max: 100
                }
            };
            app.config = {
                rotate: 90,
                align: 'left',
                verticalAlign: 'middle',
                position: 'insideBottom',
                distance: 15,
                onChange: function () {
                    const labelOption = {
                        rotate: app.config.rotate,
                        align: app.config.align,
                        verticalAlign: app.config.verticalAlign,
                        position: app.config.position,
                        distance: app.config.distance
                    };
                    myChart.setOption({
                        series: [
                            {
                                label: labelOption
                            },
                            {
                                label: labelOption
                            },
                            {
                                label: labelOption
                            },
                            {
                                label: labelOption
                            }
                        ]
                    });
                }
            };
            const labelOption = {
                show: true,
                position: app.config.position,
                distance: app.config.distance,
                align: app.config.align,
                verticalAlign: app.config.verticalAlign,
                rotate: app.config.rotate,
                formatter: '{c}  {name|{a}}',
                fontSize: 16,
                rich: {
                    name: {}
                }
            };
            let option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                legend: {
                    data: ['上午', '下午', '夜晚']
                },
                toolbox: {
                    show: true,
                    orient: 'vertical',
                    left: 'right',
                    top: 'center',
                    feature: {
                        mark: { show: true },
                        dataView: { show: true, readOnly: false },
                        magicType: { show: true, type: ['line', 'bar', 'stack'] },
                        restore: { show: true },
                        saveAsImage: { show: true }
                    }
                },
                xAxis: [
                    {
                        type: 'category',
                        axisTick: { show: false },
                        data: Data.map(x => x.Room.Name)
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        axisLabel: {
                            formatter: '{value} %' // 在这里设置 y 轴标签的格式，添加单位
                        }
                    }
                ],
                series: [
                    {
                        name: '上午',
                        type: 'bar',
                        barGap: 0,
                        label: labelOption,
                        emphasis: {
                            focus: 'series'
                        },
                        data: Data.map(x => x.AmUseRate)
                    },
                    {
                        name: '下午',
                        type: 'bar',
                        label: labelOption,
                        emphasis: {
                            focus: 'series'
                        },
                        data: Data.map(x => x.PmUseRate)
                    },
                    {
                        name: '夜晚',
                        type: 'bar',
                        label: labelOption,
                        emphasis: {
                            focus: 'series'
                        },
                        data: Data.map(x => x.NmUseRate)
                    },

                ]
            };

            myChart.setOption(option);// 渲染页面
            //随着屏幕大小调节图表
            window.addEventListener("resize", () => {
                myChart.resize();
            });
        },
        //统计各类数据到看板
        async GetDataCollect() {
            let { Data } = await this.$Post('/AppointRecord/GetDataCollect', {});
            this.DataCollect = Data;

        }
    }
}       
</script>


<style scoped>
.board-list {
    display: flex;


}

.board-list .board-item {
    text-align: center;
    padding: 20px;
    border-radius: 10px;
    color: white;
    margin-right: 20px;
    width: 180px;

}

.board-list .board-item:nth-child(1) {
    background-color: rgb(10, 146, 146);
}

.board-list .board-item:nth-child(2) {
    background-color: rgb(115, 48, 160);
}

.board-list .board-item:nth-child(3) {
    background-color: rgb(247, 173, 37);
}

.board-list .board-item:nth-child(4) {
    background-color: rgb(39, 204, 141);
}

.board-list .board-item:nth-child(5) {
    background-color: rgb(182, 32, 162);
}

.board-list .board-item:nth-child(6) {
    background-color: rgb(163, 40, 77);
}

.board-list .board-item:nth-child(7) {
    background-color: rgb(79, 72, 168);
}

.board-list .board-item .tit {}

.board-list .board-item .num {
    margin-top: 10px;
    font-weight: bolder;
}
</style>
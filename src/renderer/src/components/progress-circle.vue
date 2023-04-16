<template>
    <div class="progress">
      <el-progress
        :width="props.size"
        :hidden="props.size"
        type="circle"
        :percentage="progress()"
        :color="percentageStyle.bar_color"
        :stroke-width="props.barSize"
      >
        <span class="percentage-label">{{ props.percent }}</span>
        <span class="percentage-label">{{ progress() }}</span>
      </el-progress>
    </div>
</template>

<script setup>
import {ref} from "vue";

const props = defineProps({
  size: {
    type: Number,
    default: 100
  },
  percent: {
    type: Number,
    default: 0
  },
  barSize: {
    type: Number,
    default: 10
  },
})
const percentageStyle = {
  track_color:"#40717b",
  bar_color:"#52c5d1",
}
const curTime = ()=>{
  let hour = props.percent / 1440
  let min = props.percent % 1440
}

const progress = ()=>{
  if(props.percent>1440){
    percentageStyle.track_color = "#52c5d1"
    percentageStyle.bar_color = "#FCDE38FF"
    return (((props.percent % 1440) / 1440)*100).toFixed(2)
  }else {
    percentageStyle.track_color = "#40717b"
    percentageStyle.bar_color = "#52c5d1"
    return ((props.percent / 1440) * 100).toFixed(2)
  }

}



</script>
<style scoped>
.percentage-label {
  display: block;
  margin-top: 10px;
  font-size: 12px;
}

:deep(.el-progress path:first-child){
 stroke: v-bind('percentageStyle.track_color');
}

</style>

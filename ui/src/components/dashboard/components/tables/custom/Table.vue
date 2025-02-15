<template>
    <template v-if="generated.length">
        <el-table :id="containerID" :data="paginatedData">
            <el-table-column
                v-for="(column, index) in Object.keys(props.chart.data.columns)"
                :key="index"
                :label="column"
            >
                <template #default="scope">
                    {{ scope.row[column] }}
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :total="generated.length"
            @current-change="handlePageChange"
            @size-change="handlePageSizeChange"
            layout="prev, pager, next, sizes"
            :page-sizes="[5, 10, 20, 50]"
            :pager-count="5"
            class="mt-3"
        />
    </template>

    <NoData v-else :text="t('custom_dashboard_empty')" />
</template>

<script lang="ts" setup>
    import {onMounted, computed, ref, watch} from "vue";

    import {useI18n} from "vue-i18n";
    const {t} = useI18n({useScope: "global"});

    import NoData from "../../../../layout/NoData.vue";

    import {useStore} from "vuex";
    const store = useStore();

    import moment from "moment";

    import {useRoute} from "vue-router";
    const route = useRoute();

    defineOptions({inheritAttrs: false});
    const props = defineProps({
        identifier: {type: Number, required: true},
        chart: {type: Object, required: true},
    });

    const containerID = `${props.chart.id}__${Math.random()}`;

    const dashboard = computed(() => store.state.dashboard.dashboard);

    const currentPage = ref(1);
    const pageSize = ref(5);

    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * pageSize.value;
        return generated.value.slice(start, start + pageSize.value);
    });

    const handlePageChange = (page) => {
        currentPage.value = page;
    };

    const handlePageSizeChange = (size) => {
        pageSize.value = size;
    };

    const generated = ref([]);
    const generate = async () => {
        const params = {
            id: dashboard.value.id,
            chartId: props.chart.id,
            startDate: route.query.timeRange
                ? moment()
                    .subtract(
                        moment.duration(route.query.timeRange).as("milliseconds"),
                    )
                    .toISOString(true)
                : route.query.startDate ||
                    moment()
                        .subtract(moment.duration("PT720H").as("milliseconds"))
                        .toISOString(true),
            endDate: route.query.timeRange
                ? moment().toISOString(true)
                : route.query.endDate || moment().toISOString(true),
        };

        generated.value = await store.dispatch("dashboard/generate", params);
    };

    watch(route, async () => await generate());
    watch(
        () => props.identifier,
        () => generate(),
    );
    onMounted(() => generate());
</script>

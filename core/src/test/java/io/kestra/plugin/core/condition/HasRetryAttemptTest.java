package io.kestra.plugin.core.condition;

import com.google.common.collect.ImmutableMap;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.models.executions.TaskRun;
import io.kestra.core.models.executions.TaskRunAttempt;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.models.flows.State;
import io.kestra.core.services.ConditionService;
import io.kestra.core.utils.TestsUtils;
import io.kestra.core.junit.annotations.KestraTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@KestraTest
class HasRetryAttemptTest {
    @Inject
    ConditionService conditionService;

    @Test
    void test() {
        Flow flow = TestsUtils.mockFlow();
        Execution execution = TestsUtils.mockExecution(flow, ImmutableMap.of());

        execution = execution.withTaskRunList(List.of(TaskRun.builder()
            .attempts(List.of(
                TaskRunAttempt.builder()
                    .state(new State().withState(State.Type.KILLED))
                    .build(),
                TaskRunAttempt.builder()
                    .state(new State().withState(State.Type.SUCCESS))
                    .build()
                ))
            .build()
        ));

        HasRetryAttempt build = HasRetryAttempt.builder()
            .in(Collections.singletonList(State.Type.KILLED))
            .build();

        boolean test = conditionService.isValid(build, flow, execution);

        assertThat(test, is(true));

        build = HasRetryAttempt.builder()
            .in(Collections.singletonList(State.Type.FAILED))
            .build();

        test = conditionService.isValid(build, flow, execution);

        assertThat(test, is(false));
    }

    @Test
    void onlyOne() {
        Flow flow = TestsUtils.mockFlow();
        Execution execution = TestsUtils.mockExecution(flow, ImmutableMap.of());

        execution = execution.withTaskRunList(List.of(TaskRun.builder()
            .attempts(List.of(
                TaskRunAttempt.builder()
                    .state(new State().withState(State.Type.KILLED))
                    .build()
            ))
            .build()
        ));

        HasRetryAttempt build = HasRetryAttempt.builder()
            .build();

        boolean test = conditionService.isValid(build, flow, execution);

        assertThat(test, is(false));
    }
}
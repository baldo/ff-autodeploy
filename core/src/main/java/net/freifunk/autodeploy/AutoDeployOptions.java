/*
 * Freifunk Auto Deployer
 * Copyright (C) 2013, 2014 by Andreas Baldeau <andreas@baldeau.net>
 *
 *
 * For contributers see file CONTRIB.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *
 * Uses Logback (http://logback.qos.ch/) which is dual licensed under EPL v1.0 and LGPL v2.1.
 * See http://logback.qos.ch/license.html for details.
 */
package net.freifunk.autodeploy;

import static net.freifunk.autodeploy.AutoDeployOptions.Command.RUN_PHASES;
import static net.freifunk.autodeploy.AutoDeployOptions.Command.SHOW_FIRMWARE_LIST;
import static net.freifunk.autodeploy.AutoDeployOptions.Command.SHOW_HELP;
import static net.freifunk.autodeploy.AutoDeployOptions.Command.SHOW_MODEL_LIST;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Options for running the auto deployment.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class AutoDeployOptions {

    private final List<Command> _commands;
    private final Map<Phase, PhaseOptions> _phases;

    /**
     * Command to perform.
     *
     * @author Andreas Baldeau <andreas@baldeau.net>
     */
    public static enum Command {

        SHOW_HELP,
        SHOW_FIRMWARE_LIST,
        SHOW_MODEL_LIST,
        RUN_PHASES,
        ;
    }

    private AutoDeployOptions(final Command command) {
        this(ImmutableList.of(command), null);
    }

    private AutoDeployOptions(final List<Command> commands) {
        this(commands, null);
    }

    private AutoDeployOptions(
        final Command command,
        final Set<PhaseOptions> phases
    ) {
        this(ImmutableList.of(command), phases);
    }

    private AutoDeployOptions(
        final List<Command> commands,
        final Set<PhaseOptions> phases
    ) {
        Preconditions.checkArgument(
            commands.contains(RUN_PHASES) == (phases != null),
            "Phases must be set if the RUN_PHASES command is set."
        );
        _commands = commands;

        if (phases == null) {
            _phases = ImmutableMap.of();
        } else {
            _phases = Maps.uniqueIndex(phases, PhaseOptions.GET_PHASE);
        }
    }

    /**
     * Show help.
     */
    public static AutoDeployOptions forHelp() {
        return new AutoDeployOptions(SHOW_HELP);
    }

    /**
     * Show the specified listings.
     */
    public static AutoDeployOptions forListings(
        final boolean listFirmwares,
        final boolean listModels
    ) {
        final Builder<Command> commands = ImmutableList.builder();
        if (listFirmwares) {
            commands.add(SHOW_FIRMWARE_LIST);
        }
        if (listModels) {
            commands.add(SHOW_MODEL_LIST);
        }
        return new AutoDeployOptions(commands.build());
    }

    /**
     * Run the given phases.
     */
    public static AutoDeployOptions forPhases(
        final Set<PhaseOptions> phases
    ) {
        return new AutoDeployOptions(RUN_PHASES, phases);
    }

    public boolean shallPerform(final Command command) {
        return _commands.contains(command);
    }

    public boolean arePhasesSpecified() {
        return !_phases.isEmpty();
    }

    public boolean hasPhase(final Phase phase) {
        return _phases.get(phase) != null;
    }

    public PhaseOptions getPhaseOptions(final Phase phase) {
        final PhaseOptions options = _phases.get(phase);
        Preconditions.checkArgument(
            options != null,
            "Unknown phase " + phase + ". User hasPhase() to check if phase is configured."
        );
        return options;
    }
}

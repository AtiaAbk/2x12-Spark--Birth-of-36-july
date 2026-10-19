package bd.spark36.character;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import bd.spark36.level.DialogueRepository;
import bd.spark36.world.TerrainSurface;

/**
 * Individual Student NPC entity on the University of Dhaka campus.
 * Features:
 * - Unique identifier and historical student profile.
 * - Autonomous AI state machine: IDLE, WALKING, LOOKING_AROUND, SOCIALIZING, GROUPING, LISTENING, CHANTING, FLEEING, INTERACTING_PLAYER.
 * - Dedicated zero-allocation CharacterPose evaluated with Phase 7 anatomical kinematics.
 * - Waypoint-following and Dijkstra path navigation.
 * - Contextual Bengali and English dialogue interactions.
 */
public class StudentNPC implements bd.spark36.interaction.Interactable {

    private final int id;
    private final String roleTitle;
    private final int gender; // 0 = Male, 1 = Female
    private final int variant; // 1 .. StudentMesh.getVariantCount() - 1
    private int activeLevelNumber = 1;

    // Spatial & Kinematic State
    private final Vector3 position = new Vector3();
    private final Vector3 lastPosition = new Vector3();
    private final Vector3 velocity = new Vector3();
    private float heading = 0f; // degrees
    private float targetHeading = 0f;
    private float baseSpeed = 1.30f;
    private float currentSpeed = 0f;
    private float speedPhase = 0f;
    private float walkCycle = 0f;

    // AI Behavioral State Machine
    private NpcBehaviorState state = NpcBehaviorState.IDLE;
    private NpcBehaviorState previousState = NpcBehaviorState.IDLE;
    private float stateTimer = 0f;
    private float stateDuration = 3.0f;
    private float chatCooldown = 0f;
    private float stuckTime = 0f;
    private StudentNPC socialPartner = null;
    private final Vector3 groupFocalPoint = new Vector3();
    private boolean hasGroupFocalPoint = false;

    // Navigation State
    private int currentEdge = 0;
    private int fromNode = 0;
    private int toNode = 1;
    private float lane = 0f;
    private final int[] plannedPath = new int[CampusWaypointGraph.NODES.length];
    private int pathLength = 0;
    private int pathIndex = 0;

    // Phase 7 Articulated Character Pose (Zero Heap Allocation)
    private final CharacterPose pose = new CharacterPose();

    // Dialogue State
    private DialogueRepository.DialogueLine activeDialogue = null;
    private int levelNumber = 1;

    public StudentNPC(int id, int gender, int variant, float baseSpeed, String roleTitle) {
        this.id = id;
        this.gender = (gender == 1) ? 1 : 0;
        this.variant = Math.max(1, variant);
        this.baseSpeed = baseSpeed;
        this.currentSpeed = baseSpeed;
        this.speedPhase = MathUtils.random(MathUtils.PI2);
        this.roleTitle = roleTitle != null ? roleTitle : (this.gender == 1 ? "DU Female Student" : "DU Male Student");
        this.chatCooldown = MathUtils.random(5f, 25f);
        this.pose.reset();
    }

    /**
     * Updates NPC state machine, path movement, avoidance steering, and pose kinematics.
     */
    public void update(float delta, CampusWaypointGraph graph, Vector3 playerPos, float levelDelta) {
        if (delta > 0.1f) delta = 0.1f;
        stateTimer += delta;
        if (chatCooldown > 0f) chatCooldown -= delta;

        // 1. Behavioral State Machine Logic
        switch (state) {
            case IDLE:
                currentSpeed = Math.max(0f, currentSpeed - 4f * delta);
                if (stateTimer >= stateDuration) {
                    transitionTo(NpcBehaviorState.WALKING, MathUtils.random(8f, 18f));
                }
                break;

            case LOOKING_AROUND:
                currentSpeed = Math.max(0f, currentSpeed - 4f * delta);
                if (stateTimer >= stateDuration) {
                    transitionTo(NpcBehaviorState.WALKING, MathUtils.random(10f, 22f));
                }
                break;

            case SOCIALIZING:
                currentSpeed = Math.max(0f, currentSpeed - 4f * delta);
                if (socialPartner != null) {
                    turnTowards(socialPartner.position.x - position.x, socialPartner.position.z - position.z, delta, 5f);
                }
                if (stateTimer >= stateDuration) {
                    endSocializing();
                }
                break;

            case LISTENING:
            case CHANTING:
                currentSpeed = Math.max(0f, currentSpeed - 3f * delta);
                if (hasGroupFocalPoint) {
                    turnTowards(groupFocalPoint.x - position.x, groupFocalPoint.z - position.z, delta, 4f);
                }
                break;

            case INTERACTING_PLAYER:
                currentSpeed = Math.max(0f, currentSpeed - 5f * delta);
                if (playerPos != null) {
                    turnTowards(playerPos.x - position.x, playerPos.z - position.z, delta, 7f);
                }
                if (stateTimer >= stateDuration) {
                    endInteraction();
                }
                break;

            case WALKING:
            case GROUPING:
            case FLEEING:
                updateWalkingMovement(delta, graph, playerPos);
                break;
        }

        // 2. Evaluate Articulated Character Kinematics into pose
        evaluatePose(delta);
    }

    private void updateWalkingMovement(float delta, CampusWaypointGraph graph, Vector3 playerPos) {
        // Dynamic pace drift
        float speedMultiplier = (state == NpcBehaviorState.FLEEING) ? 1.75f :
                                (state == NpcBehaviorState.GROUPING) ? 1.25f : 1.0f;
        float targetSpeed = baseSpeed * speedMultiplier * (1f + 0.08f * MathUtils.sin(speedPhase += delta * 0.4f));
        currentSpeed += (targetSpeed - currentSpeed) * Math.min(1f, 1.8f * delta);

        float[] a = CampusWaypointGraph.NODES[fromNode];
        float[] b = CampusWaypointGraph.NODES[toNode];
        float sx = b[0] - a[0], sz = b[1] - a[1];
        float len = (float) Math.sqrt(sx * sx + sz * sz);
        if (len < 0.001f) len = 0.001f;
        float nx = -sz / len, nz = sx / len;
        float tx = b[0] + nx * lane, tz = b[1] + nz * lane;

        float dx = tx - position.x, dz = tz - position.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (dist < 0.6f) {
            advanceToNextWaypoint(graph);
            return;
        }

        float vx = dx / dist * currentSpeed;
        float vz = dz / dist * currentSpeed;
        velocity.set(vx, 0f, vz);

        lastPosition.set(position);

        // Move with solid collision testing
        moveWithCollision(graph, vx * delta, vz * delta);

        // Stuck detection
        if (position.dst2(lastPosition) < (currentSpeed * delta * 0.2f) * (currentSpeed * delta * 0.2f)) {
            stuckTime += delta;
            if (stuckTime > 2.2f) {
                stuckTime = 0f;
                // Reverse edge
                enterEdge(graph, currentEdge, toNode);
            }
        } else {
            stuckTime = 0f;
        }

        // Elevation support from stairs/plinths + terrain surface
        float groundY = Math.max(graph.supportHeight(position.x, position.z),
                                 TerrainSurface.getGroundHeight(position.x, position.z));
        position.y += (groundY - position.y) * Math.min(1f, 12f * delta);

        // Walk cycle cadence scaled to physical speed
        walkCycle += (currentSpeed / 1.45f) * MathUtils.PI2 * delta;
        turnTowards(vx, vz, delta, 5.5f);
    }

    private void advanceToNextWaypoint(CampusWaypointGraph graph) {
        if (pathIndex < pathLength) {
            // Follow planned Dijkstra path
            int nextNode = plannedPath[pathIndex++];
            int edge = findEdgeBetween(fromNode, nextNode);
            if (edge != -1) {
                enterEdge(graph, edge, toNode);
            } else {
                pickRandomNextEdge(graph);
            }
        } else {
            // Destination reached or roaming freely
            if (state == NpcBehaviorState.GROUPING) {
                transitionTo(NpcBehaviorState.LISTENING, MathUtils.random(15f, 40f));
            } else if (state == NpcBehaviorState.FLEEING) {
                transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(6f, 12f));
            } else {
                // Chance to pause and observe or socialize
                float roll = MathUtils.random();
                if (roll < 0.22f) {
                    transitionTo(NpcBehaviorState.LOOKING_AROUND, MathUtils.random(2.5f, 6.0f));
                } else if (roll < 0.35f) {
                    transitionTo(NpcBehaviorState.IDLE, MathUtils.random(2.0f, 5.0f));
                } else {
                    pickRandomNextEdge(graph);
                }
            }
        }
    }

    private void pickRandomNextEdge(CampusWaypointGraph graph) {
        int[][] nodeEdges = graph.getNodeEdges();
        if (toNode >= nodeEdges.length) return;
        int[] options = nodeEdges[toNode];
        if (options == null || options.length == 0) return;

        int next = currentEdge;
        if (options.length > 1) {
            do {
                next = options[MathUtils.random(options.length - 1)];
            } while (next == currentEdge);
        }
        enterEdge(graph, next, toNode);
    }

    private int findEdgeBetween(int u, int v) {
        for (int e = 0; e < CampusWaypointGraph.EDGES.length; e++) {
            int a = CampusWaypointGraph.EDGES[e][0];
            int b = CampusWaypointGraph.EDGES[e][1];
            if ((a == u && b == v) || (a == v && b == u)) return e;
        }
        return -1;
    }

    public void enterEdge(CampusWaypointGraph graph, int e, int fromNodeId) {
        this.currentEdge = e;
        this.fromNode = fromNodeId;
        this.toNode = (CampusWaypointGraph.EDGES[e][0] == fromNodeId) ?
            CampusWaypointGraph.EDGES[e][1] : CampusWaypointGraph.EDGES[e][0];

        float[][] edgeLanes = graph.getEdgeLanes();
        if (e < edgeLanes.length && edgeLanes[e].length > 0) {
            float[] lanes = edgeLanes[e];
            float l = lanes[MathUtils.random(lanes.length - 1)];
            this.lane = (CampusWaypointGraph.EDGES[e][0] == fromNodeId) ? l : -l;
        } else {
            this.lane = 0f;
        }
    }

    /** Plans a shortest path to a target waypoint node using Dijkstra routing. */
    public void routeToNode(CampusWaypointGraph graph, int targetNode) {
        pathLength = graph.findPath(this.toNode, targetNode, plannedPath);
        pathIndex = 0;
    }

    public void moveWithCollision(CampusWaypointGraph graph, float mx, float mz) {
        if (graph.insideSolid(position.x + mx, position.z, CampusWaypointGraph.BODY_RADIUS) == null) {
            position.x += mx;
        }
        if (graph.insideSolid(position.x, position.z + mz, CampusWaypointGraph.BODY_RADIUS) == null) {
            position.z += mz;
        }

        // Shove resolution if already overlapping
        float[] b = graph.insideSolid(position.x, position.z, CampusWaypointGraph.BODY_RADIUS);
        if (b != null) {
            float r = CampusWaypointGraph.BODY_RADIUS;
            float left = position.x + r - b[0], right = b[3] - (position.x - r);
            float back = position.z + r - b[2], front = b[5] - (position.z - r);
            float m = Math.min(Math.min(left, right), Math.min(back, front));
            if (m == left) position.x -= left;
            else if (m == right) position.x += right;
            else if (m == back) position.z -= back;
            else position.z += front;
        }
    }

    public void applyAvoidanceForce(float fx, float fz, float delta) {
        position.x += fx * baseSpeed * delta;
        position.z += fz * baseSpeed * delta;
    }

    private void turnTowards(float dx, float dz, float delta, float rate) {
        if (dx * dx + dz * dz < 1e-6f) return;
        targetHeading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
        float diff = (targetHeading - heading) % 360f;
        if (diff > 180f) diff -= 360f;
        if (diff < -180f) diff += 360f;
        heading += diff * Math.min(1f, rate * delta);
    }

    /**
     * Evaluates anatomical pose degrees of freedom with Phase 7 kinematic fidelity.
     */
    private void evaluatePose(float delta) {
        pose.reset();

        float breathe = MathUtils.sin(stateTimer * 2.2f + id * 0.7f) * 0.005f;
        pose.breathe = breathe;
        pose.pelvisHeight = 0.87f;

        boolean isMoving = state.isMoving() && currentSpeed > 0.15f;

        if (isMoving) {
            float swing = MathUtils.sin(walkCycle);
            float bobAmp = (state == NpcBehaviorState.FLEEING) ? 0.035f : 0.020f;
            pose.bobOffset = Math.abs(swing) * 0.020f;
            pose.pelvisRoll = swing * 2.2f;
            pose.pelvisTwist = swing * 3.5f;
            pose.torsoTwist = -swing * 4.0f;
            pose.torsoPitch = (state == NpcBehaviorState.FLEEING) ? 12.0f : 4.0f;

            for (int side = 0; side < 2; side++) {
                float s = (side == 0) ? swing : -swing;
                pose.thighPitch[side] = s * 30f;
                pose.thighRoll[side] = (side == 0 ? -1f : 1f) * 1.5f;
                pose.kneePitch[side] = (s < 0f) ? -s * 38f : 4f;
                pose.footPitch[side] = (s < 0f) ? -s * 14f : -s * 10f;

                float armDir = (side == 0) ? -swing : swing;
                pose.upperArmPitch[side] = 3f + armDir * 26f;
                pose.upperArmRoll[side] = (side == 0 ? 3.5f : -3.5f);
                pose.foreArmPitch[side] = -16f - Math.max(0f, armDir) * 14f;
            }
        } else {
            // Stationary Behavioral Poses
            switch (state) {
                case LOOKING_AROUND:
                    pose.headYaw = MathUtils.sin(stateTimer * 1.2f) * 26f;
                    pose.headPitch = MathUtils.sin(stateTimer * 0.8f) * 7f;
                    break;

                case SOCIALIZING:
                case INTERACTING_PLAYER: {
                    float nod = MathUtils.sin(stateTimer * 2.8f) * 5.0f;
                    pose.headPitch = nod;
                    float gesture = MathUtils.sin(stateTimer * 1.8f);
                    pose.upperArmPitch[0] = -12f + gesture * 14f;
                    pose.foreArmPitch[0] = -42f + gesture * 22f;
                    break;
                }

                case CHANTING: {
                    float chantPhase = MathUtils.sin(stateTimer * 4.0f);
                    pose.upperArmPitch[1] = -85f + chantPhase * 12f; // Right arm raised
                    pose.foreArmPitch[1] = -18f;
                    pose.torsoPitch = 5.0f + chantPhase * 3.5f;
                    break;
                }

                case LISTENING:
                    pose.torsoPitch = 2.0f;
                    pose.headPitch = -4.0f;
                    break;

                case IDLE:
                default:
                    // Subtle organic fidget
                    if (stateTimer > 4.0f) {
                        pose.headYaw = MathUtils.sin(stateTimer * 0.6f) * 10f;
                    }
                    break;
            }
        }

        // Lightweight Foot Elevation Offset based on ground height
        float groundL = TerrainSurface.getGroundHeight(position.x - 0.12f, position.z);
        float groundR = TerrainSurface.getGroundHeight(position.x + 0.12f, position.z);
        float groundC = TerrainSurface.getGroundHeight(position.x, position.z);
        pose.footGroundOffset[0] = MathUtils.clamp(groundL - groundC, -0.2f, 0.2f);
        pose.footGroundOffset[1] = MathUtils.clamp(groundR - groundC, -0.2f, 0.2f);
    }

    public void transitionTo(NpcBehaviorState newState, float duration) {
        this.previousState = this.state;
        this.state = newState;
        this.stateTimer = 0f;
        this.stateDuration = duration;
    }

    public void startSocializing(StudentNPC partner, float duration) {
        this.socialPartner = partner;
        this.chatCooldown = duration + MathUtils.random(15f, 35f);
        transitionTo(NpcBehaviorState.SOCIALIZING, duration);
    }

    public void endSocializing() {
        this.socialPartner = null;
        transitionTo(NpcBehaviorState.WALKING, MathUtils.random(8f, 20f));
    }

    public void startPlayerInteraction(int activeLevel) {
        this.levelNumber = activeLevel;
        this.activeDialogue = DialogueRepository.getStudentDialogue(levelNumber, id);
        transitionTo(NpcBehaviorState.INTERACTING_PLAYER, 5.0f);
    }

    public void endInteraction() {
        this.activeDialogue = null;
        transitionTo(previousState != NpcBehaviorState.INTERACTING_PLAYER ? previousState : NpcBehaviorState.IDLE, 3.0f);
    }

    public void setGroupFocalPoint(Vector3 focalPoint) {
        if (focalPoint != null) {
            this.groupFocalPoint.set(focalPoint);
            this.hasGroupFocalPoint = true;
        } else {
            this.hasGroupFocalPoint = false;
        }
    }

    public void render(ModelBatch batch, Environment env, StudentMesh mesh) {
        mesh.render(batch, env, position, heading, pose, false, false, gender, variant);
    }

    // Getters
    public int getId() { return id; }
    public String getRoleTitle() { return roleTitle; }
    public int getGender() { return gender; }
    public int getVariant() { return variant; }
    public Vector3 getPosition() { return position; }
    public Vector3 getVelocity() { return velocity; }
    public float getHeading() { return heading; }
    public float getSpeed() { return currentSpeed; }
    public float getBaseSpeed() { return baseSpeed; }
    public NpcBehaviorState getState() { return state; }
    public float getStateTimer() { return stateTimer; }
    public CharacterPose getPose() { return pose; }
    public float getChatCooldown() { return chatCooldown; }
    public StudentNPC getSocialPartner() { return socialPartner; }
    public DialogueRepository.DialogueLine getActiveDialogue() { return activeDialogue; }
    public boolean isInteractable() { return state.isInteractable(); }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public void setHeading(float headingDeg) {
        this.heading = headingDeg;
        this.targetHeading = headingDeg;
    }

    public void setLevelNumber(int levelNumber) {
        this.activeLevelNumber = levelNumber;
    }

    // =========================================================================
    // INTERACTABLE INTERFACE IMPLEMENTATION
    // =========================================================================

    @Override
    public Vector3 getInteractionPosition() {
        return position;
    }

    @Override
    public float getInteractionRadius() {
        return 2.6f;
    }

    @Override
    public String getInteractionPrompt() {
        return "[E] TALK TO " + (roleTitle != null ? roleTitle.toUpperCase() : "DU STUDENT");
    }

    @Override
    public boolean canInteract(Vector3 playerPos, float playerHeading) {
        return isInteractable() && state != NpcBehaviorState.INTERACTING_PLAYER;
    }

    @Override
    public boolean onInteract(Vector3 playerPos, PlayerController player, bd.spark36.hud.WhereWindsMeetHUD hud) {
        if (!isInteractable() || state == NpcBehaviorState.INTERACTING_PLAYER) return false;

        bd.spark36.dialogue.DialogueSequence seq = DialogueRepository.getStudentDialogueSequence(activeLevelNumber, id);
        seq.setCallback(new bd.spark36.dialogue.DialogueCallback() {
            @Override
            public void onDialogueComplete(String sequenceId) {
                endInteraction();
            }
        });

        startPlayerInteraction(activeLevelNumber);
        if (playerPos != null) {
            turnTowards(playerPos.x - position.x, playerPos.z - position.z, 1f, 100f);
        }
        if (hud != null) {
            hud.startDialogueSequence(seq);
            bd.spark36.audio.SoundManager.get().playWhoosh();
        }
        return true;
    }

    @Override
    public int getInteractionPriority() {
        return 2;
    }

    @Override
    public String getInteractableId() {
        return "StudentNPC_" + id;
    }
}

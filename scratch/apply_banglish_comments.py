import os
import re

FILE_DESCRIPTIONS = {
    # launcher
    "DesktopLauncher.java": "ei file ta desktop e game launch korar main entry point",
    "StartupHelper.java": "ei file ta mac ar onno os e jvm ar lwjgl setup thik kore",

    # core main
    "SparkGame.java": "ei file ta shob level, rendering ar game loop eksathe chalay",
    "DialoguePrefs.java": "ei file ta dialogue settings ar preferences save kore",
    "GameProfile.java": "ei file ta player profile ar settings load ar save kore",
    "GameSettings.java": "ei file ta graphics, sound ar gameplay settings er data rakhe",
    "SaveData.java": "ei file ta player er progress ar unlock kora level save kore",
    "SaveManager.java": "ei file ta game er save file manage kore ar disk e lekhe",
    "SaveStorage.java": "ei file ta json file theke save data read ar write kore",
    "SettingsApplier.java": "ei file ta settings onujayi game er volume ar display update kore",
    "AmbienceSynth.java": "ei file ta campus er batash ar natural shobdo synthesize kore",
    "SoundManager.java": "ei file ta game er shob sound effects ar background audio play kore",
    "CinematicCamera.java": "ei file ta third person ar cinematic camera movement control kore",
    "BoundedLruCache.java": "ei file ta screenshot thumbnail gular jonno lru memory cache banay",
    "CaptureController.java": "ei file ta photo mode ar screenshot tolar input handle kore",
    "CaptureToast.java": "ei file ta screenshot tolar por screen e notification dekhay",
    "FolderOpener.java": "ei file ta operating system er file manager e screenshot folder khole",
    "FrameGrabber.java": "ei file ta opengl frame buffer theke pixel capture kore image banay",
    "GalleryIndex.java": "ei file ta tola screenshot gular list ar metadata track kore",
    "GalleryScreen.java": "ei file ta player er tola shob chobi gallery view te dekhay",
    "PhotoMode.java": "ei file ta photo mode er camera angle ar hud hide kora manage kore",
    "ScreenshotService.java": "ei file ta screenshot file disk e png hishebe save kore",
    "ScrollInput.java": "ei file ta gallery screen e mouse scroll input process kore",
    "AnimationStateMachine.java": "ei file ta character er running, walking ar combat animation blend kore",
    "CampusCrowd.java": "ei file ta campus e student der path dhore shavabik chola fira koray",
    "CampusWaypointGraph.java": "ei file ta campus er walkway gulate navigation waypoint graph banay",
    "CharacterPose.java": "ei file ta character er joint angle ar skeletal pose define kore",
    "HelmetGoon.java": "ei file ta helmet pora attacker goon der ai ar movement control kore",
    "LocomotionState.java": "ei file ta character er haata ar douranor movement state rakhe",
    "NpcBehaviorState.java": "ei file ta student npc der idle, walk ar talking state control kore",
    "PlayerController.java": "ei file ta keyboard ar mouse diye main player ke control kore",
    "RigMerger.java": "ei file ta character er bibhinno body part mesh eksathe merge kore",
    "StudentMesh.java": "ei file ta procedural 3d student model ar dress mesh toiri kore",
    "StudentNPC.java": "ei file ta campus er student npc der identity ar dialogue manage kore",
    "FrameStats.java": "ei file ta game er fps, frame time ar performance stats hishab kore",
    "GlStats.java": "ei file ta opengl er draw call, triangle ar texture memory track kore",
    "DialogueCallback.java": "ei file ta dialogue shesh hole trigger kora callback interface",
    "DialogueManager.java": "ei file ta screen e bangla dialogue text ar choices present kore",
    "DialogueSequence.java": "ei file ta ektar por ekta dialogue line er sequence manage kore",
    "SourceCatalog.java": "ei file ta july movement er aitihashik tothyo ar source catalog rakhe",
    "FontRenderer.java": "ei file ta screen e sundor bangla ar english font render kore",
    "WhereWindsMeetHUD.java": "ei file ta compass, mission objective ar gameplay hud draw kore",
    "CreditsContent.java": "ei file ta game er credits ar credits text data provide kore",
    "CreditsScreen.java": "ei file ta game er credit screen render kore",
    "DialogueText.java": "ei file ta dialogue box er bangla text ar style format kore",
    "HistoricalContextCard.java": "ei file ta campus e aitihashik ghotona shomporke card popup dekhay",
    "LoadingScreen.java": "ei file ta level load howar shomoy loading screen dekhay",
    "MainMenuScreen.java": "ei file ta game shuru korar main menu screen render kore",
    "SettingsScreen.java": "ei file ta settings menu render kore jekhane player option change kore",
    "GameInput.java": "ei file ta keyboard ar mouse er shob input event capture kore",
    "Interactable.java": "ei file ta player jader sathe kotha bolte ba interact korte pare tader interface",
    "InteractionSystem.java": "ei file ta kache thaka object ba npc er sathe interaction handle kore",
    "CampaignLevel.java": "ei file ta story campaign er shob level er base class",
    "CheckpointStore.java": "ei file ta level er checkpoint save ar restore kore",
    "CivilianStrikeRule.java": "ei file ta nirdosh student der marle fail howar rule check kore",
    "DialogueRepository.java": "ei file ta shob level er bangla dialogue data store kore",
    "Level1PeacefulCampus.java": "ei file ta curzon hall er shantopurno campus exploration level",
    "Level2ShelterObjectives.java": "ei file ta level 2 er mission objectives ar task track kore",
    "Level2StudentConfrontation.java": "ei file ta campus e student der prothom shonghat er level",
    "Level3PoliceEscalation.java": "ei file ta police escalation ar tear gas shomoyer level",
    "Level4CurfewBlackout.java": "ei file ta curfew ar internet blackout er moddhe level",
    "Level5MassMobilization.java": "ei file ta lakho manusher mass mobilization er level",
    "Level6VictoryEpilogue.java": "ei file ta movement er bijoy ar epilogue er shesh level",
    "LevelConfig.java": "ei file ta active level, difficulty ar game mode config kore",
    "LevelManager.java": "ei file ta level switch ar level life cycle manage kore",
    "LevelPbrMaterials.java": "ei file ta level er pbr material ar texture bind kore",
    "StreetDressing.java": "ei file ta dhakar rasta gulate rickshaw ar barricade sajiye dey",
    "MissionObjective.java": "ei file ta ekti nirdishto mission objective er status rakhe",
    "MissionTracker.java": "ei file ta shob active objective track kore ar update kore",
    "ObjectiveState.java": "ei file ta objective active, completed naki failed seta rakhe",
    "ObjectiveType.java": "ei file ta objective er category jemon talk, reach ba defend define kore",
    "GraphicsTier.java": "ei file ta low, medium ar high graphics quality tier set kore",
    "HdrPostProcessor.java": "ei file ta bloom, color grading ar hdr tone mapping apply kore",
    "PbrEnvironment.java": "ei file ta realistic sunlight, sky ibl ar ambient lighting banay",
    "RenderPipelineConfig.java": "ei file ta 3d render pipeline er settings ar buffer manage kore",
    "WaterRenderer.java": "ei file ta curzon hall er pukurer realistic pani shader diye render kore",
    "AtmosphereConfig.java": "ei file ta akasher rong, fog ar daylight parameters configure kore",
    "AtmosphereRenderer.java": "ei file ta akasher shundor daylight gradient ar megh render kore",
    "DhakaCampusWorld.java": "ei file ta curzon hall campus er 3d world, building ar ground toiri kore",
    "GladeGeometry.java": "ei file ta bash jhar, pather pathor ar gash er jhar toiri kore",
    "GroundConfig.java": "ei file ta campus er matir texture, ghonotto ar height configure kore",
    "JulyMemorials.java": "ei file ta curzon hall e july shahid der shritishoudho banay",
    "LilyPad.java": "ei file ta pukurer panir upor shapla ar pad render kore",
    "ParticleSystem.java": "ei file ta dhula, batash ar leaf particle effect toiri kore",
    "PerimeterEnclosure.java": "ei file ta campus er boundary wall ar boundary gachpala banay",
    "PostProcessor.java": "ei file ta screen er post process effect combine kore",
    "StaticSceneBatch.java": "ei file ta static 3d model chunk gulo ke optimize kore render kore",
    "TerrainSurface.java": "ei file ta campus er matir slope ar rastar surface generate kore",
    "TextureFactory.java": "ei file ta procedural texture jemon eet, pata ar bark generate kore",
    "TreeEntity.java": "ei file ta ekti gach er model, batasher nora ar lod handle kore",
    "TreeGeometry.java": "ei file ta procedural gach, dalpala ar lush pata toiri kore",
    "VegetationConfig.java": "ei file ta gachpala, batash ar pata er size config kore",
    "VegetationManager.java": "ei file ta campus er shob gach manage ar update kore",
    "WaterConfig.java": "ei file ta panir dheu, shosshota ar rong configure kore",
    "WaterWaveModel.java": "ei file ta panir dheu er physics hishab kore",
    "WeatherController.java": "ei file ta brishti, batash ar weather change control kore",
    "WeatherState.java": "ei file ta current weather er state information store kore",
    "WindModel.java": "ei file ta gachpala noranor jonno batasher physics model banay",
    "BarricadeBuilder.java": "ei file ta rastar upor basher ar lohar barricade model banay",
    "BengaliSignAtlas.java": "ei file ta rastar billboard ar dokaner bangla signboard banay",
    "BusBuilder.java": "ei file ta dhakar local bus er 3d model generate kore",
    "CngAutoRickshawBuilder.java": "ei file ta sobuj ronger cng auto rickshaw 3d model banay",
    "ElectricPoleCableBuilder.java": "ei file ta rastar pasher current er khuti ar tar toiri kore",
    "MilitaryVehicleBuilder.java": "ei file ta curfew shomoyer military apc gari model toiri kore",
    "OpeningBuilder.java": "ei file ta curzon hall er daroan ar gate er structure banay",
    "RickshawBuilder.java": "ei file ta dhakar traditional rickshaw er 3d model banay",
    "ShaheedMinarBuilder.java": "ei file ta kendriyo shaheed minar er 3d model generate kore",
    "ShopfrontBuilder.java": "ei file ta campus er pasher tong dokan ar shopfront banay",
    "StreetPropKit.java": "ei file ta rastar lamp post, dustbin ar street prop toiri kore",
    "TeaStallBuilder.java": "ei file ta dhaka university er cha er tong dokan generate kore",

    # tests
    "SaveManagerTest.java": "ei file ta save manager er save ar load functionality test kore",
    "SettingsApplyTest.java": "ei file ta game settings application logic test kore",
    "BoundedLruCacheTest.java": "ei file ta bounded lru cache er memory limit test kore",
    "GalleryIndexTest.java": "ei file ta photo gallery er indexing test kore",
    "PhotoModeTest.java": "ei file ta photo mode er camera ar hud toggle test kore",
    "ScreenshotServiceTest.java": "ei file ta screenshot file save howar service test kore",
    "CharacterAnimationTest.java": "ei file ta character animation state machine test kore",
    "NpcCrowdTest.java": "ei file ta student npc crowd movement test kore",
    "FrameStatsTest.java": "ei file ta performance frame stats calculation test kore",
    "SourceCatalogTest.java": "ei file ta aitihashik source catalog data test kore",
    "CreditsContentTest.java": "ei file ta credits screen er text content test kore",
    "LoadingAndMenuScreenTest.java": "ei file ta loading screen ar menu screen logic test kore",
    "GameInputAndLevelFlowTest.java": "ei file ta input handling ar level progression test kore",
    "InteractionMissionTest.java": "ei file ta npc interaction ar mission tracking test kore",
    "CheckpointTest.java": "ei file ta level checkpoint save ar restore test kore",
    "CivilianStrikeDisqualificationTest.java": "ei file ta civilian der marle disqualify howar rule test kore",
    "Level2NonLethalCompletionTest.java": "ei file ta level 2 non lethal bhabe complete kora test kore",
    "PostProcessTest.java": "ei file ta post processing shader buffer test kore",
    "AtmosphereTest.java": "ei file ta daylight atmosphere ar fog configuration test kore",
    "BengaliSignTextTest.java": "ei file ta bangla signboard texture generation test kore",
    "RoadClearanceAndWaterTest.java": "ei file ta rasta thik thaka ar pani clearance test kore",
    "SummerEnvironmentAndExitTest.java": "ei file ta summer environment ar safe exit logic test kore",
    "TerrainTest.java": "ei file ta campus terrain surface generation test kore",
    "VegetationTest.java": "ei file ta gachpala ar foliage data structure test kore",
    "WaterWaveModelTest.java": "ei file ta water wave simulation model test kore"
}

def strip_java_comments(source):
    result = []
    i = 0
    n = len(source)
    while i < n:
        if source[i:i+3] == '\"\"\"':
            start = i
            i += 3
            while i + 2 < n and source[i:i+3] != '\"\"\"':
                if source[i] == '\\':
                    i += 2
                else:
                    i += 1
            i += 3
            result.append(source[start:i])
        elif source[i] == '\"':
            start = i
            i += 1
            while i < n and source[i] != '\"':
                if source[i] == '\\':
                    i += 2
                else:
                    i += 1
            if i < n:
                i += 1
            result.append(source[start:i])
        elif source[i] == '\'':
            start = i
            i += 1
            while i < n and source[i] != '\'':
                if source[i] == '\\':
                    i += 2
                else:
                    i += 1
            if i < n:
                i += 1
            result.append(source[start:i])
        elif source[i:i+2] == '//':
            i += 2
            while i < n and source[i] != '\n':
                i += 1
        elif source[i:i+2] == '/*':
            i += 2
            while i + 1 < n and source[i:i+2] != '*/':
                i += 1
            i += 2
        else:
            result.append(source[i])
            i += 1
    return ''.join(result)

def clean_and_format(code, top_comment):
    # Ensure lowercase single line
    top_comment_clean = "// " + top_comment.strip().lower()
    
    lines = code.split('\n')
    cleaned = []
    prev_blank = False
    for line in lines:
        trimmed = line.rstrip()
        if not trimmed:
            if not prev_blank:
                cleaned.append('')
                prev_blank = True
        else:
            cleaned.append(trimmed)
            prev_blank = False
            
    while cleaned and not cleaned[0]:
        cleaned.pop(0)
    while cleaned and not cleaned[-1]:
        cleaned.pop()
        
    return top_comment_clean + "\n" + '\n'.join(cleaned) + "\n"

def process_file(filepath):
    filename = os.path.basename(filepath)
    desc = FILE_DESCRIPTIONS.get(filename)
    if not desc:
        base_name = filename.replace('.java', '').lower()
        desc = f"ei file ta {base_name} er kaj kore"
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    no_comments = strip_java_comments(content)
    final_code = clean_and_format(no_comments, desc)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(final_code)
    print(f"Processed: {filepath}")

def main():
    root_dirs = ["core/src", "lwjgl3/src"]
    total = 0
    for root_dir in root_dirs:
        for root, dirs, files in os.walk(root_dir):
            for file in sorted(files):
                if file.endswith(".java"):
                    full_path = os.path.join(root, file)
                    process_file(full_path)
                    total += 1
    print(f"\nCompleted {total} files successfully.")

if __name__ == "__main__":
    main()

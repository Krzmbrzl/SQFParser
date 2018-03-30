//determine if instance is a JIP player
AAS_JIPplayer = not isServer && isNull player;
AAS_IsPlayerAdmin = serverCommandAvailable "#kick";

//determine location of scripts
AAS_PathToCore = "";
if (isNil "AAS_PathToCore") then {AAS_PathToCore = "ca\blitzkrieg_f\";};

#include "configuration\Type.hpp"
#include "settings\MissionSettings.hpp"
#include "settings\ZoneSetup.sqf"
#include "configuration\convertDefines.sqf";

call compile preprocessFileLineNumbers "configuration\SetParameters.sqf";
call compile preprocessFileLineNumbers (AAS_PathToCore + "core\init.sqf");

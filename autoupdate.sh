#!/bin/bash

#This script gets executed as part of the update routine for the bot. It updates the jar file and then executes it.
#Note that this is a very simple script, mainly intended for development purposes; it's far from perfect.

#The directory in which the bot's update file is located
update_location=~/baggybot.jar
#The directory in which the bot is located
bot_location=~/bot/baggybot.jar

if [ -f $update ];
then
    if diff $update_location $bot_location > /dev/null;
    then
        mv $update_location $bot_location
        java -Xmx128M -Xms64M -jar baggybot.jar -update sameversion
    else
        mv $update_location $bot_location
        java -Xmx128M -Xms64M -jar baggybot.jar -update success
    fi
else
    java -Xmx128M -Xms64M -jar baggybot.jar -update nofile
fi

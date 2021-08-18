#!/bin/sh

mkdir classes
clj -M -e "(compile 'laurazard.drapeau.api)"
clj -M:uberdeps --main-class laurazard.drapeau.api
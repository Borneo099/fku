#!/bin/bash
set -e

mkdir -p ~/.m2/repository/net/minecraftforge

deps=(
    "srgutils:0.5.10"
    "DiffPatch:2.0.12"
    "JarJarMetadata:0.3.19"
    "JarJarSelector:0.3.19"
    "JarJarTransformer:0.3.19"
)

for dep in "${deps[@]}"; do
    name=$(echo $dep | cut -d: -f1)
    version=$(echo $dep | cut -d: -f2)
    dir=~/.m2/repository/net/minecraftforge/$name/$version
    mkdir -p $dir
    echo "Downloading $name:$version..."
    curl -sL "https://maven.minecraftforge.net/net/minecraftforge/$name/$version/$name-$version.pom" -o $dir/$name-$version.pom
    curl -sL "https://maven.minecraftforge.net/net/minecraftforge/$name/$version/$name-$version.jar" -o $dir/$name-$version.jar
done

echo "All dependencies downloaded to ~/.m2/repository"
ls -la ~/.m2/repository/net/minecraftforge/

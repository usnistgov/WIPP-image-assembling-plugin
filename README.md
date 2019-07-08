# README

## WIPP Image Assembling plugin

Reads multiple stitching vectors generated from [MIST](https://github.com/usnistgov/MIST) and creates one tiled TIFF image from tiled TIFF images per stitching vector.

## Docker distribution

This plugin is available on [DockerHub from the WIPP organization](https://hub.docker.com/r/wipp/wipp-image-assembling-plugin)
```shell
docker pull wipp/wipp-image-assembling-plugin
```

## Running the Docker container

```shell
docker run \
    -v "path/to/input/data/folder":/data/inputs \
    -v "path/to/output/folder":/data/outputs \
    wipp-image-assembling-plugin \
    --inputImages /data/inputs/"inputCollectionTiledFolder"  
    --inputStitchingVector /data/inputs/"stitchingVectorFolder" 
    --output /data/outputs
 ```   
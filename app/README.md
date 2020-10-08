# Helix Java API Demo

## Table of Contents
- Description
- Running

### Description
This is a simple demo how to utilize Helix Java API. 
This example demonstrates how to integrate the Helix Java API into a 3rd-party vendor application.

### Running
A base Dockerfile is provided on `{repo}/Dockerfile`.
You should build it, and label it `helix-java:latest`. You can name it as
you wish, but the remainder of these instructions assume that the base has that
name.

Once the base is available, you can either modify it, or run it as is.

The utilities' directory supplied under `util/` has the up to date 
utility scripts that will aid you in running your environment seamlessly. 

After building Docker runtime image, create inside of it the following mount points:
1. the utilities' directory to /helix/util/
2. the java demo artifacts directory to /helix/java/demo/

```
docker run -e JPDA_ADDRESS=*:7777 -e JPDA_TRANSPORT=dt_socket 
-v /path/to/repo/app/artifacts/:/helix/java/demo/ 
-v /path/to/repo/util/:/helix/util/ 
-p 7777:7777 -it helix-java bash
```

JPDA_ADDRESS (for remote debugging) is set to 7777, and the port is exposed for it.

When you're on the container, you can run the demos as follows:
1. `cd /helix`
2. `./util/symlinks.sh` (should only really only be done once on container start)
3. `./util/run.sh` (you can modify this script as desired)
4. `diff client_text.txt client_text.txt-restored.helix` (show that there are no differences between original and decrypted contents)

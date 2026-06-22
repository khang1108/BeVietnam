"""Render a glTF/GLB model as a transparent 360 turntable PNG sequence.

Run headless with Blender (Cycles CPU works on a GPU-less VM):

    blender --background --python scripts/bake_turntable.py -- \
        <model.gltf|glb> <out_dir> [frames] [samples]

Outputs <out_dir>/frame_0001.png ... as RGBA with a transparent background.
"""

import math
import sys

import bpy

argv = sys.argv[sys.argv.index("--") + 1:]
model_path = argv[0]
out_dir = argv[1].rstrip("/")
frames = int(argv[2]) if len(argv) > 2 else 120
samples = int(argv[3]) if len(argv) > 3 else 32

# Clean default scene.
bpy.ops.wm.read_factory_settings(use_empty=True)
scene = bpy.context.scene

# Import the model.
if model_path.lower().endswith(".glb") or model_path.lower().endswith(".gltf"):
    bpy.ops.import_scene.gltf(filepath=model_path)
else:
    raise SystemExit(f"Unsupported model: {model_path}")

meshes = [o for o in scene.objects if o.type == "MESH"]
if not meshes:
    raise SystemExit("No mesh imported")

# Parent all meshes to a single empty so we can spin them as one rig.
spin = bpy.data.objects.new("Spin", None)
scene.collection.objects.link(spin)
for m in meshes:
    if m.parent is None:
        m.parent = spin

# World bounding box of all meshes -> center + radius for framing.
coords = []
for m in meshes:
    for corner in m.bound_box:
        coords.append(m.matrix_world @ __import__("mathutils").Vector(corner))
min_c = [min(c[i] for c in coords) for i in range(3)]
max_c = [max(c[i] for c in coords) for i in range(3)]
center = [(min_c[i] + max_c[i]) / 2 for i in range(3)]
radius = max((max_c[i] - min_c[i]) for i in range(3)) / 2 or 1.0

# Camera, angled slightly down, distance scaled to fit the bounds.
cam_data = bpy.data.cameras.new("Cam")
cam = bpy.data.objects.new("Cam", cam_data)
scene.collection.objects.link(cam)
dist = radius * 3.2
cam.location = (center[0], center[1] - dist, center[2] + radius * 0.9)
# Aim at center.
import mathutils  # noqa: E402

direction = mathutils.Vector(center) - cam.location
cam.rotation_euler = direction.to_track_quat("-Z", "Y").to_euler()
scene.camera = cam

# Even lighting: bright world + a key sun. No tint -> alpha stays clean.
world = bpy.data.worlds.new("W")
world.use_nodes = True
world.node_tree.nodes["Background"].inputs[1].default_value = 1.2
scene.world = world
sun_data = bpy.data.lights.new("Sun", type="SUN")
sun_data.energy = 3.0
sun = bpy.data.objects.new("Sun", sun_data)
sun.rotation_euler = (math.radians(55), 0, math.radians(35))
scene.collection.objects.link(sun)

# Render settings: transparent film, Cycles CPU, square-ish RGBA PNG.
scene.render.engine = "CYCLES"
scene.cycles.device = "CPU"
scene.cycles.samples = samples
scene.cycles.use_denoising = True
scene.render.film_transparent = True
scene.render.resolution_x = 1024
scene.render.resolution_y = 1024
scene.render.image_settings.file_format = "PNG"
scene.render.image_settings.color_mode = "RGBA"

# Spin a full turn across the frame range and render each frame.
spin.location = (-center[0], -center[1], -center[2])  # recenter rig on origin
for f in range(frames):
    spin.rotation_euler = (0, 0, 2 * math.pi * f / frames)
    scene.render.filepath = f"{out_dir}/frame_{f + 1:04d}.png"
    bpy.ops.render.render(write_still=True)
    print(f"[bake] frame {f + 1}/{frames}")

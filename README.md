# SF2 Portrait Manager 64

** tool for managing character portraits in Shining Force II (Sega Genesis / Mega Drive).**

SF2 Portrait Manager 64 is a specialized Java application designed for importing, editing, and exporting character portraits while maintaining full compatibility with the original 64-color format used in the game. The program preserves the exact tile structure, color order, and internal data format (including the 48-byte disassembly format), ensuring 100% compatibility with ROM files and patches.

### Key Features
- **Supported formats**:
  - Import/Export: PNG (indexed and RGB), GIF
  - Direct work with disassembly format (48-byte SF2 portrait format)
  - Support for additional metadata (`.meta` files)
- Automatic 64-color palette generation and strict preservation (6-bit depth, index 0 = transparent)
- Tile-based processing: 64 tiles of 8×8 pixels (64×64 portrait)
- Full graphical user interface (GUI) for easy navigation through tiles, palette, and metadata
- Support for bitplane structure (4 standard bitplanes + extended bits 4 and 5)

### In-Game Portrait Format
Each portrait consists of 64 tiles (8×8 pixels) and uses a shared 64-color palette.  
In ROM the data is stored in a compact 48-byte format:
- Bytes 0–3 → standard 4 bitplanes (bits 0–3)
- Byte 4 → bitplane 5
- Byte 5 → bitplane 4

### Build and Run

1. Install **JDK 8 or higher** (JDK 11/17 recommended).
2. Clone the repository:
   ```bash
   git clone https://github.com/Slagar78/SF2PortraitManager-64.git

Build with Apache Ant:Bashant jarThe compiled JAR will be placed in the dist/ folder.
Alternative: open the project in NetBeans (nbproject folder is already included).

Running the Application
Bashjava -jar dist/SF2PortraitManager-64.jar
Usage

Launch the program.
Import: load a PNG/GIF or disassembly file — the tool automatically converts it to the 64-color tiled format.
Edit: modify individual tiles, palette, and metadata via the intuitive GUI.
Export: save back to PNG/GIF or to the original 48-byte disassembly format + .meta.

Project Structure
textsrc/com/sfc/sf2/
├── portrait/      ← Core portrait logic (Portrait, PortraitManager)
├── graphics/      ← Graphics handling (GraphicsManager, Tile)
├── palette/       ← 64-color palette management
├── gui/           ← Graphical user interface
├── io/            ← Input/Output (PNG, GIF, disassembly)
└── layout/        ← UI layout

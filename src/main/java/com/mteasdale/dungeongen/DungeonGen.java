package com.mteasdale.dungeongen;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Teasdale on 2/19/2017.
 * Adapted from https://github.com/munificent/hauberk
 * 
 * The random dungeon generator.
 *
 * Starting with a stage of solid walls, it works like so:
 *
 * 1. Place a number of randomly sized and positioned rooms. If a room
 *    overlaps an existing room, it is discarded. Any remaining rooms are
 *    carved out.
 * 2. Any remaining solid areas are filled in with mazes. The maze generator
 *    will grow and fill in even odd-shaped areas, but will not touch any
 *    rooms.
 * 3. The result of the previous two steps is a series of unconnected rooms
 *    and mazes. We walk the stage and find every tile that can be a
 *    "connector". This is a solid tile that is adjacent to two unconnected
 *    regions.
 * 4. We randomly choose connectors and open them or place a door there until
 *    all of the unconnected regions have been joined. There is also a slight
 *    chance to carve a connector between two already-joined regions, so that
 *    the dungeon isn't single connected.
 * 5. The mazes will have a lot of dead ends. Finally, we remove those by
 *    repeatedly filling in any open tile that's closed on three sides. When
 *    this is done, every corridor in a maze actually leads somewhere.
 *
 * The end result of this is a multiply-connected dungeon with rooms and lots
 * of winding corridors.
 */
public class DungeonGen {
    private int mapXSpan;
    private int mapYSpan;
    private int numRoomTries; // Try to put this many rooms onto the map.
    private int triesPerRoom;
    private List<StringBuilder> mapStringList = new ArrayList<>();

    public static final char STONE = '0';
    public static final char OPEN = '1';
    public static final char WALL = '2';
    private static final Logger LOG = LoggerFactory.getLogger(DungeonGen.class);

    // The inverse chance of adding a connector between two regions that have
    // already been joined. Increasing this leads to more loosely connected
    // dungeons.
    int extraConnectorChance = 20;


    int windingPercent = 0;

    List<Rectangle2D> roomList = new ArrayList<>();

    /// For each open position in the dungeon, the index of the connected region
    /// that that position is a part of.
    // Array2D<int> _regions;

    /// The index of the current region being carved.
    int currentRegion = -1;

    public DungeonGen(int mapXSpan, int mapYSpan, int numRoomTries, int triesPerRoom) {
        this.mapXSpan = mapXSpan;
        this.mapYSpan = mapYSpan;
        this.numRoomTries = numRoomTries;
        this.triesPerRoom = triesPerRoom;
        // Fill the map with stone. We'll carve out rooms and corridors later.
        for (int y = 0; y < mapYSpan; y++) {
            StringBuilder rowString = new StringBuilder();
            for (int x = 0; x < mapXSpan; x++) {
                rowString.append(STONE);
            }
            this.mapStringList.add(rowString);
        }
    }

    public void generate() {
        /*
        if (stage.width % 2 == 0 || stage.height % 2 == 0) {
            throw new ArgumentError("The stage must be odd-sized.");
        }

        bindStage(stage);

        fill(Tiles.wall);
        _regions = new Array2D(stage.width, stage.height);
        */

        addRooms();
        /*
        for (StringBuilder s : mapStringList) {
            System.out.println(s.toString());
        }
        */

/*
        // Fill in all of the empty space with mazes.
        for (var y = 1; y < bounds.height; y += 2) {
            for (var x = 1; x < bounds.width; x += 2) {
                var pos = new Vec(x, y);
                if (getTile(pos) != Tiles.wall) continue;
                _growMaze(pos);
            }
        }
*/
        /*

        _connectRegions();
        _removeDeadEnds();

        _rooms.forEach(onDecorateRoom);
        */
    }
    /*
    void onDecorateRoom(Rect room) {}

    /// Implementation of the "growing tree" algorithm from here:
    /// http://www.astrolog.org/labyrnth/algrithm.htm.
    private void growMaze(Point2D start) {
        List<Point2D> cells = new ArrayList<>();
        var lastDir;

        _startRegion();
        _carve(start);

        cells.add(start);
        while (cells.isNotEmpty) {
            var cell = cells.last;

            // See which adjacent cells are open.
            var unmadeCells = <Direction>[];

            for (var dir in Direction.CARDINAL) {
                if (_canCarve(cell, dir)) unmadeCells.add(dir);
            }

            if (unmadeCells.isNotEmpty) {
                // Based on how "windy" passages are, try to prefer carving in the
                // same direction.
                var dir;
                if (unmadeCells.contains(lastDir) && rng.range(100) > windingPercent) {
                    dir = lastDir;
                } else {
                    dir = rng.item(unmadeCells);
                }

                _carve(cell + dir);
                _carve(cell + dir * 2);

                cells.add(cell + dir * 2);
                lastDir = dir;
            } else {
                // No adjacent uncarved cells.
                cells.removeLast();

                // This path has ended.
                lastDir = null;
            }
        }
    }
*/
    // Places rooms.
    private void addRooms() {
        for (int i = 0; i < numRoomTries; i++) {
            LOG.info("Room #{}", i);
            // Pick a random room size.
            // Might want to replace this with a random selection of pre-designed rooms.
            int xSpan = (int)(Math.random() * 7 + 2.5); // Should generate a number between 2 and 8.
            int ySpan = (int)(Math.random() * 7 + 2.5); // Should generate a number between 2 and 8.
            LOG.info("  Width: {}, Height: {}", xSpan, ySpan);

            // Try to place this room.
            Rectangle2D room = null;
            for (int t = 0; t < triesPerRoom; t++) {
                // Pick a location somewhere within the confines of the map with 0 <= x < mapXSpan
                int x = (int) (Math.random() * (mapXSpan - xSpan));
                int y = (int) (Math.random() * (mapYSpan - ySpan));
                room = new Rectangle2D(x, y, (double) xSpan, (double) ySpan);

                // See if this position overlaps another room.
                boolean overlaps = false;
                // Give this room a 1 grid space around it to separate it from other rooms.
                double x1 = (x < 1) ? 0 : x - 1;
                double y1 = (y < 1) ? 0 : y - 1;
                double xSpan1 = (x < 1) ? xSpan + 1 : xSpan + 2;
                double ySpan1 = (y < 1) ? ySpan + 1 : ySpan + 2;
                Rectangle2D roomAndWalls = new Rectangle2D(x1, y1, xSpan1, ySpan1);
                for (Rectangle2D otherRoom : roomList) {
                    if (roomAndWalls.intersects(otherRoom)) {
                        overlaps = true;
                        break;
                    }
                }

                if (overlaps) {
                    LOG.info("  ({},{}) Rejected! Overlaps.", x, y);
                } else {
                    roomList.add(room);
                    // Mark out this room on the map.
                    for (int row = (int) room.getMinY(); row < (int) room.getMaxY(); row++ ) {
                        StringBuilder mapRow = mapStringList.get(row);
                        for (int col = (int) room.getMinX(); col < (int) room.getMaxX(); col++) {
                            mapRow.setCharAt(col, OPEN);
                        }
                    }
                    break;
                }
            }

            /*
            _startRegion();
            */
        }
        for (StringBuilder s : mapStringList) {
            LOG.info("Row: {}, Contents: {}", mapStringList.indexOf(s), s.toString());
        }
    }
    
    /*
    void _connectRegions() {
        // Find all of the tiles that can connect two (or more) regions.
        var connectorRegions = <Vec, Set<int>>{};
        for (var pos in bounds.inflate(-1)) {
            // Can't already be part of a region.
            if (getTile(pos) != Tiles.wall) continue;

            var regions = new Set<int>();
            for (var dir in Direction.CARDINAL) {
                var region = _regions[pos + dir];
                if (region != null) regions.add(region);
            }

            if (regions.length < 2) continue;

            connectorRegions[pos] = regions;
        }

        var connectors = connectorRegions.keys.toList();

        // Keep track of which regions have been merged. This maps an original
        // region index to the one it has been merged to.
        var merged = {};
        var openRegions = new Set<int>();
        for (var i = 0; i <= _currentRegion; i++) {
            merged[i] = i;
            openRegions.add(i);
        }

        // Keep connecting regions until we're down to one.
        while (openRegions.length > 1) {
            var connector = rng.item(connectors);

            // Carve the connection.
            _addJunction(connector);

            // Merge the connected regions. We'll pick one region (arbitrarily) and
            // map all of the other regions to its index.
            var regions = connectorRegions[connector]
                    .map((region) => merged[region]);
            var dest = regions.first;
            var sources = regions.skip(1).toList();

            // Merge all of the affected regions. We have to look at *all* of the
            // regions because other regions may have previously been merged with
            // some of the ones we're merging now.
            for (var i = 0; i <= _currentRegion; i++) {
                if (sources.contains(merged[i])) {
                    merged[i] = dest;
                }
            }

            // The sources are no longer in use.
            openRegions.removeAll(sources);

            // Remove any connectors that aren't needed anymore.
            connectors.removeWhere((pos) {
                    // Don't allow connectors right next to each other.
            if (connector - pos < 2) return true;

            // If the connector no long spans different regions, we don't need it.
            var regions = connectorRegions[pos].map((region) => merged[region])
            .toSet();

            if (regions.length > 1) return false;

            // This connecter isn't needed, but connect it occasionally so that the
            // dungeon isn't singly-connected.
            if (rng.oneIn(extraConnectorChance)) _addJunction(pos);

            return true;
      });
        }
    }

    void _addJunction(Vec pos) {
        if (rng.oneIn(4)) {
            setTile(pos, rng.oneIn(3) ? Tiles.openDoor : Tiles.floor);
        } else {
            setTile(pos, Tiles.closedDoor);
        }
    }

    void _removeDeadEnds() {
        var done = false;

        while (!done) {
            done = true;

            for (var pos in bounds.inflate(-1)) {
                if (getTile(pos) == Tiles.wall) continue;

                // If it only has one exit, it's a dead end.
                var exits = 0;
                for (var dir in Direction.CARDINAL) {
                    if (getTile(pos + dir) != Tiles.wall) exits++;
                }

                if (exits != 1) continue;

                done = false;
                setTile(pos, Tiles.wall);
            }
        }
    }

    /// Gets whether or not an opening can be carved from the given starting
    /// [Cell] at [pos] to the adjacent Cell facing [direction]. Returns `true`
    /// if the starting Cell is in bounds and the destination Cell is filled
    /// (or out of bounds).</returns>
    bool _canCarve(Vec pos, Direction direction) {
        // Must end in bounds.
        if (!bounds.contains(pos + direction * 3)) return false;

        // Destination must not be open.
        return getTile(pos + direction * 2) == Tiles.wall;
    }

    void _startRegion() {
        _currentRegion++;
    }

    void _carve(Vec pos, [TileType type]) {
        if (type == null) type = Tiles.floor;
        setTile(pos, type);
        _regions[pos] = _currentRegion;
    }
    */

    public List<StringBuilder> getMapStringList() {
        return mapStringList;
    }
}
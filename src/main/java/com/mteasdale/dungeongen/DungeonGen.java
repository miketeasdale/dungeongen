package com.mteasdale.dungeongen;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
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
    private Rectangle2D mapBounds= null;
    private int numRoomTries; // Try to put this many rooms onto the map.
    private int triesPerRoom;
    private TileMap tileMap;

    public static final char STONE = '0';
    public static final char OPEN = '1';
    public static final char PASSAGE = '2';
    private static final Logger LOG = LoggerFactory.getLogger(DungeonGen.class);

    // The inverse chance of adding a connector between two regions that have
    // already been joined. Increasing this leads to more loosely connected
    // dungeons.
    int extraConnectorChance = 20;

    int windingPercent = 0;

    List<Rectangle2D> roomList = new ArrayList<>();

    /// For each open position in the dungeon, the index of the connected region
    /// that that position is a part of.
    // Regions is another variable similar to the mapStringList.
    List<List<Integer>> regions = null;
    //Array2D<int> _regions;

    /// The index of the current region being carved.
    int currentRegion = -1;

    public DungeonGen(int mapXSpan, int mapYSpan, int numRoomTries, int triesPerRoom) {
        if (mapXSpan % 2 != 0 || mapYSpan % 2 != 0) {
            LOG.error("Map size must be even numbers.");
        } else {
            this.mapBounds = new Rectangle2D(0, 0, mapYSpan, mapXSpan);
            this.numRoomTries = numRoomTries;
            this.triesPerRoom = triesPerRoom;
            this.tileMap = new TileMap((int) mapBounds.getWidth(), (int) mapBounds.getHeight());
        }
    }

    public void generate() {
        addRooms();

        // Fill in all of the empty space with mazes.
        for (int y = 1; y < mapBounds.getMaxX(); y += 2) {
            for (int x = 1; x < mapBounds.getMaxY(); x += 2) {
                Point2D pos = new Point2D(x, y);
                if (tileMap.getContent(pos) == STONE) growMaze(pos);
            }
        }

        /*

        _connectRegions();
        _removeDeadEnds();

        _rooms.forEach(onDecorateRoom);
        */
    }
    /*
    void onDecorateRoom(Rect room) {}
    */

    /// Implementation of the "growing tree" algorithm from here:
    /// http://www.astrolog.org/labyrnth/algrithm.htm.
    private void growMaze(Point2D start) {
        List<Point2D> cells = new ArrayList<>();
        // A vector holding the last direction taken by this path.
        Point2D lastDir = null;
        // Set up a list of vectors that correspond to the 4 cardinal directions.
        List<Point2D> directionList = new ArrayList<>();
        directionList.add(new Point2D(-1, 0));
        directionList.add(new Point2D(0, -1));
        directionList.add(new Point2D(1, 0));
        directionList.add(new Point2D(0, 1));

        startRegion();
        carve(start, OPEN);

        cells.add(start);
        while (!cells.isEmpty()) {
            // Get last cell added.
            Point2D cell = cells.get(cells.size() - 1);

            // Make a list of adjacent cells that can be carved out.
            List<Point2D> unmadeCells = new ArrayList<>();

            for (Point2D dir : directionList) {
                if (canCarve(cell, dir)) unmadeCells.add(dir);
            }

            if (!unmadeCells.isEmpty()) {
                // Based on how "windy" passages are, try to prefer carving in the
                // same direction.
                Point2D dir;
                if (unmadeCells.contains(lastDir) && Rng.getRandomIn(1, 100) > windingPercent) {
                    dir = lastDir;
                } else {
                    // Select a direction from the list of possible ones.
                    dir = directionList.get(Rng.getRandomIn(0, unmadeCells.size()));
                }

                carve(cell.add(dir), PASSAGE);
                carve(cell.add(dir.multiply(2)), PASSAGE);

                cells.add(cell.add(dir.multiply(2)));
                lastDir = dir;
            } else {
                // No adjacent uncarved cells.
                cells.remove(cells.size());

                // This path has ended.
                lastDir = null;
            }
        }
    }

    // Places rooms.
    private void addRooms() {
        for (int i = 0; i < numRoomTries; i++) {
            LOG.info("Room #{}", i);
            // Pick a random room size.
            // Might want to replace this with a random selection of pre-designed rooms.
            Point2D roomSize = makeRoom();

            // Try to place this room.
            Rectangle2D room = null;
            for (int t = 0; t < triesPerRoom; t++) {
                // Pick a location somewhere within the confines of the map with 0 <= x < mapXSpan
                int x = Rng.getRandomIn(1, (int)(mapBounds.getMaxX() - roomSize.getX()) / 2) * 2 + 1;
                int y = Rng.getRandomIn(1, (int)(mapBounds.getMaxY() - roomSize.getY()) / 2) * 2 + 1;
                room = new Rectangle2D(x, y, roomSize.getX(), roomSize.getY());

                // See if this position overlaps another room.
                boolean overlaps = false;
                for (Rectangle2D otherRoom : roomList) {
                    if (room.intersects(otherRoom)) {
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
                        for (int col = (int) room.getMinX(); col < (int) room.getMaxX(); col++) {
                            carve(new Point2D(col, row), OPEN);
                        }
                    }
                    break;
                }
            }
           startRegion();
        }
    }
    
    // Returns a 2D vector containing room dimensions.
    private Point2D makeRoom() {
        // Pick a random room size. The funny math here does two things:
        // - It makes sure rooms are odd-sized to line up with maze.
        // - It avoids creating rooms that are too rectangular: too tall and
        //   narrow or too wide and flat.
        // TODO: This isn't very flexible or tunable. Do something better here.
        int size = Rng.getRandomIn(1, 3) * 2 + 1;
        int rectangularity = Rng.getRandomIn(0, 1 + size / 2) * 2;
        int width = size;
        int height = size;
        if (Rng.oneIn(2)) {
            width += rectangularity;
        } else {
            height += rectangularity;
        }
        LOG.info("  Width: {}, Height: {}", width, height);
        return new Point2D((double) width, (double) height);
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
*/
    /// Gets whether or not an opening can be carved from the given starting
    /// [Cell] at [pos] to the adjacent Cell facing [direction]. Returns `true`
    /// if the starting Cell is in bounds and the destination Cell is filled
    /// (or out of bounds).
    private Boolean canCarve(Point2D pos, Point2D direction) {
        // Must end in bounds.
        Point2D testPos = pos.add(direction.multiply(3));
        if (testPos.getX() < 0 || testPos.getX() >= mapBounds.getMaxX() ) return false;
            if (testPos.getY() < 0 || testPos.getY() >= mapBounds.getMaxY() ) return false;

        // Destination must not be open.
        return tileMap.getContent(pos.add(direction.multiply(2))) == STONE;
    }
    
    private void startRegion() {
        currentRegion++;
    }

    // Sets the content of a tile, and the region it belongs to.
    private void carve(Point2D pos, char content) {
        tileMap.setContent(pos, content);
        tileMap.setRegion(pos, currentRegion);
    }

    public TileMap getTileMap(){
        return tileMap;
    }
}
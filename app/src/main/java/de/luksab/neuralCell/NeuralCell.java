package de.luksab.neuralCell;

import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class NeuralCell extends PApplet {

    float speed = 0.003f, weightToRadius = 0.007f, scaling, lostWeight = 0, cDir, cSpeed;
    public int p = 1;
    public ArrayList<Cell> Cells = new ArrayList<Cell>();
    public boolean algoCells = true, cannibalism = true;

    public void setup() {
        scaling = (2 * height);
        background(0);
        if (Cells.isEmpty()) {
            Cells.add(new ControlledCell(10));
            if (algoCells)
                Cells.add(new AlgCell(5));
            float gesMass = 95;
            for (int i = 0; i < 100; i++) {
                float cell = random(1, 10);
                Cells.add(new AiCell(gesMass / cell));
                gesMass = gesMass / cell;
            }
            Cells.add(new AiCell(gesMass));
        }
    }

    public void newAlCell() {
        Cells.add(new AlgCell(5));
        lostWeight -= 5;
    }

    public void draw() {
        background(0);
        stroke(255);
        if (!algoCells) {
            for (int i = 0; i < Cells.size(); i++) {
                Cell d = Cells.get(i);
                if (d.getClass() == AlgCell.class) {
                    lostWeight += d.w;
                    d.del = true;
                }
            }
        }
        for (int i = 0; i < Cells.size(); i++) {
            Cell d = Cells.get(i);
            if (d.getClass() == ControlledCell.class) {
                ControlledCell c = (ControlledCell) d;
                c.d = cDir;
                c.s = cSpeed;
            }
        }

        for (int k = 0; k < p; k++) {
            if (lostWeight > 5) {
                for (int i = 0; i < lostWeight; i++) {
                    Cells.add(new AiCell(0.8f));
                    lostWeight--;
                }
            }
            if (lostWeight > 2) {
                Cells.add(new AiCell(lostWeight));
                lostWeight = 0;
            }

            for (int i = 0; i < Cells.size(); i++) {
                Cell d = Cells.get(i);
                d.update();
                if (d.del)
                    Cells.remove(d);
            }
            for (int i = 0; i < Cells.size(); i++) {
                Cell d = Cells.get(i);
                d.update();
                if (d.split)
                    Cells.add(d.split());
            }

            for (int i = 0; i < Cells.size(); i++)
                for (int j = i + 1; j < Cells.size(); j++) {
                    Cell d0 = Cells.get(i);
                    Cell d1 = Cells.get(j);
                    if (d0.isColliding(d1)) {
                        if (d0.w > d1.w) {
                            d0.w += d1.w;
                            Cells.remove(j);
                        } else if (d1.w > d0.w) {
                            d1.w += d0.w;
                            Cells.remove(i);
                        } else {
                            d0.w += d1.w;
                            Cells.remove(j);
                        }
                    }
                }
        }
        for (int i = 0; i < Cells.size(); i++) {
            Cell d = Cells.get(i);
            d.draw();
        }
    }

    public void spawnControlled() {
        Cells.add(new ControlledCell(5));
        lostWeight -= 5;
    }

    public class AiCell extends Cell {
        public float d, s, r, g, b;
        public Net net;

        public AiCell(float weight) {
            super(weight);
            d = random(TAU);
            s = random(1);
            net = new Net();
            r = random(255);
            g = random(255);
            b = random(255);
        }

        public AiCell(float weight, float[][] l0, float[][] l1, float r, float g, float b) {
            super(weight);
            d = random(TAU);
            s = random(1);
            net = new Net();
        }

        public void updaten() {
            float[] arr = findNNearest(1);
            float[] array = new float[10];
            for (int i = 0; i < 3; i++)
                array[i] = arr[i];
            array[9] = w;
            //println(net.update(array));
            arr = net.update(array);
            d = arr[0];
            if (arr[1] < 0)
                arr[1] = -arr[1];
            if (arr[1] > 1)
                s = 1;
            else
                s = arr[1];
            if (arr[2] > 0.5f && a > 120 && w > 3) split = true;
            else split = false;
            x += speed * s * cos(d * TAU);
            y += speed * s * sin(d * TAU);
            if (x >= 1 || x <= 0)
                if (d <= PI)
                    d = PI - d;
                else
                    d = (3 * PI) - d;

            if (y >= 1 || y <= 0)
                d = TAU - d;
        }

        public Cell split() {
            w = w / 2;

            float mutation = 0.1f;
            float[][] l0 = new float[net.l0.length][net.l0[0].length];
            float[][] l1 = new float[net.l1.length][net.l1[0].length];
            for (int i = 0; i < l0.length; i++) {
                for (int j = 0; j < l0[0].length; j++)
                    l0[i][j] = net.l0[i][j] + random(-mutation, mutation);
            }
            for (int i = 0; i < l1.length; i++) {
                for (int j = 0; j < l1[0].length; j++)
                    l1[i][j] = net.l1[i][j] + random(-mutation, mutation);
            }
            return new AiCell(w, l0, l1, r + random(-1, 1), g + random(-1, 1), b + random(-1, 1));
        }

        public void draw() {
            stroke(127);
            fill(r, g, b);
            ellipse(x * height, y * height, (float) Math.sqrt(w) * weightToRadius * scaling, (float) Math.sqrt(w) * weightToRadius * scaling);
        }
    }

    public class Net {
        float[][] l0 = new float[10][10];
        float[][] l1 = new float[l0[0].length][3];

        public Net() {
            for (int i = 0; i < l0.length; i++) {
                for (int j = 0; j < l0[0].length; j++)
                    l0[i][j] = random(-1, 1);
            }
            for (int i = 0; i < l1.length; i++) {
                for (int j = 0; j < l1[0].length; j++)
                    l1[i][j] = random(-1, 1);
            }
        }

        public Net(float[][] ol0, float[][] ol1) {
            l0 = ol0;
            l1 = ol1;
        }

        public float[] update(float[] input) {
            if (input.length != l0[0].length)
                return new float[0];
            float[] layer = new float[input.length];
            for (int i = 0; i < input.length; i++) {
                for (int j = 0; j < l0[0].length; j++)
                    layer[i] += input[i] * l0[i][j];
            }
            float[] out = new float[l1[0].length];
            for (int i = 0; i < l1[0].length; i++) {
                for (int j = 0; j < l1[0].length; j++)
                    out[i] +=
                            layer[i] *
                                    l1[i][j];
            }
            return out;
        }
    }

    public class AlgCell extends Cell {
        public float d, s;

        public AlgCell(float weight) {
            super(weight);
            d = random(TAU);
            s = 1;
        }

        public float[] findNNearest(int n) {
            Cell[] nearCells = new Cell[n];
            for (int k = 0; k < Cells.size(); k++) {
                Cell c = Cells.get(k);
                if (cannibalism || !(c.getClass() == AlgCell.class))
                    if (c != this) {
                        for (int i = 0; i < n; i++) {
                            if (nearCells[i] == null) {
                                nearCells[i] = c;
                            } else if (distance(c) < distance(nearCells[i])) {
                                Cell p = nearCells[i];
                                nearCells[i] = c;
                                for (int j = i + 1; j < n; j++) {
                                    if (nearCells[j] == null) {
                                        break;
                                    }
                                    if (distance(p) < distance(nearCells[j])) {
                                        Cell f = p;
                                        p = nearCells[j];
                                        nearCells[j] = f;
                                    }
                                }
                                break;
                            }
                        }
                    }
            }
            float[] result = new float[3 * n];
            for (int i = 0; i < n; i++) {
                if (nearCells[i] != null) {
                    result[3 * i] = nearCells[i].w;
                    result[3 * i + 1] = atan((nearCells[i].y - y) / (nearCells[i].x - x));
                    if (nearCells[i].x < x) {
                        result[3 * i + 1] += PI;
                    }
                    if (result[3 * i + 1] < 0) {
                        result[3 * i + 1] += TAU;
                    }
                    result[3 * i + 2] = (float) (distance(nearCells[i]));
                }
            }
            return result;

        }

        public void updaten() {
            if (w > 10) split = true;
            else split = false;
            float[] nearCell = findNNearest(1);
            if (nearCell[0] < w) {
                d = nearCell[1];
            } else {
                d = ((nearCell[1] + PI) % TAU);
                if (x >= 0.99f && y >= 0.99f) {
                    if (d < PI / 4 || d > 3 * PI / 2) {
                        d = 3 * PI / 2;
                    } else if (d < PI / 2) {
                        d = PI;
                    }
                } else if (x <= 0.01f && y >= 0.99f) {
                    if (d < 3 * PI / 4) {
                        d = 0;
                    } else if (d < 3 * PI / 2) {
                        d = 3 * PI / 2;
                    }
                } else if (x >= 0.99f && y <= 0.01f) {
                    if (d < PI / 2 || d > 7 * PI / 4) {
                        d = PI / 2;
                    } else if (d > PI) {
                        d = PI;
                    }
                }
                if (x <= 0.01f && y <= 0.01f) {
                    if (d > 5 * PI / 7) {
                        d = 0;
                    } else if (d < PI / 2) {
                        d = PI / 2;
                    }
                } else {
                    if (x >= 0.99f) {
                        if (d < PI / 2) {
                            d = PI / 2;
                        } else if (d > 3 * PI / 2 && d != 0) {
                            d = 3 * PI / 2;
                        } else if (d == 0) {
                            if (y > 0.5f) {
                                d = 3 * PI / 2;
                            } else {
                                d = PI / 2;
                            }
                        }
                    } else if (x <= 0.01f) {
                        if (d > PI / 2 && d < PI) {
                            d = PI / 2;
                        } else if (d > PI && d < 3 * PI / 2) {
                            d = 3 * PI / 2;
                        } else if (d == PI) {
                            if (y > 0.5f) {
                                d = 3 * PI / 2;
                            } else {
                                d = PI / 2;
                            }
                        }
                    }
                    if (y >= 0.99f) {
                        if (d > PI / 2 && d < PI) {
                            d = PI;
                        } else if (d < PI / 2) {
                            d = 0;
                        } else if (d == PI / 2) {
                            if (x > 0.5f) {
                                d = PI;
                            } else {
                                d = 0;
                            }
                        }
                    } else if (y <= 0.01f) {
                        if (d < 3 * PI / 2 && d > PI) {
                            d = PI;
                        } else if (d > 3 * PI / 2) {
                            d = 0;
                        } else if (d == 3 * PI / 2) {
                            if (x > 0.5f) {
                                d = PI;
                            } else {
                                d = 0;
                            }
                        }
                    }
                }
            }
            x += speed * s * cos(d);
            y += speed * s * sin(d);
        }

        public Cell split() {
            w = w / 2;
            return new AlgCell(w);
        }

        public void draw() {
            fill(204, 200, 0);
            noStroke();
            ellipse(x * height, y * height, (float) Math.sqrt(w) * weightToRadius * scaling, (float) Math.sqrt(w) * weightToRadius * scaling);
        }
    }

    abstract class Cell {
        public float w, x, y, a, l;
        public boolean del, split = false;

        public Cell(float weight) {
            w = weight;
            x = random(1);
            y = random(1);
            l = 0.001f;
        }

        public String toString() {
            return "[" + "" + "]";
        }

        public void update() {
            a++;
            if (random(1) > 5000 / a) {
                lostWeight += w;
                del = true;
            }
            lostWeight += w * l;
            w *= (1 - l);
            if (w < 0.05f) {
                del = true;
                lostWeight += w;
            }
            if (x >= 1)
                x = 0.99f;
            else if (x <= 0)
                x = 0.01f;
            if (y >= 1)
                y = 0.99f;
            if (y <= 0)
                y = 0.01f;
            updaten();
        }

        public Cell split() {
            return new DumbCell(0);
        }

        public void updaten() {
        }

        public void draw() {
        }

        public float[] findNNearest(int n) {
            Cell[] nearCells = new Cell[n];
            for (int k = 0; k < Cells.size(); k++) {
                Cell c = Cells.get(k);
                if (c != this) {
                    for (int i = 0; i < n; i++) {
                        if (nearCells[i] == null) {
                            nearCells[i] = c;
                        } else if (distance(c) < distance(nearCells[i])) {
                            Cell p = nearCells[i];
                            nearCells[i] = c;
                            for (int j = i + 1; j < n; j++) {
                                if (nearCells[j] == null) {
                                    break;
                                }
                                if (distance(p) < distance(nearCells[j])) {
                                    Cell f = p;
                                    p = nearCells[j];
                                    nearCells[j] = f;
                                }
                            }
                            break;
                        }
                    }
                }
            }
            float[] result = new float[3 * n];
            for (int i = 0; i < n; i++) {
                if (nearCells[i] != null) {
                    result[3 * i] = nearCells[i].w;
                    result[3 * i + 1] = atan((nearCells[i].y - y) / (nearCells[i].x - x));
                    if (nearCells[i].x < x) {
                        result[3 * i + 1] += PI;
                    }
                    if (result[3 * i + 1] < 0) {
                        result[3 * i + 1] += TAU;
                    }
                    result[3 * i + 2] = (float) (distance(nearCells[i]));
                }
            }
            return result;
        }

        public double distance(Cell c) {
            return (Math.sqrt((Math.pow((x - c.x), 2)) + (Math.pow((y - c.y), 2))));
        }

        public boolean isColliding(Cell c) {
            if (c == this) return false;
            return (Math.pow((c.x - x), 2) + Math.pow((c.y - y), 2) <= Math.pow((((float) Math.sqrt(c.w) + (float) Math.sqrt(w)) * weightToRadius), 2));
        }
    }

    private class ControlledCell extends Cell {
        float d, s;
        boolean p;

        ControlledCell(float weight) {
            super(weight);
            s = 1;
            d = 0;
        }

        public void updaten() {
            /*a = 0;
            d = atan((((float) (mouseY) / height) - y) / (((float) (mouseX) / height) - x));
            if (((float) (mouseX) / height) < x) {
                d += PI;
            }
            if (d < 0) {
                d += TAU;
            }

            s = (float) (Math.sqrt((Math.pow((x - (float) (mouseX) / height), 2)) + (Math.pow((y - (float) (mouseY) / height), 2)))) * 5;
            if (s > 1)
                s = 1;
            x += speed * s * cos(d);
            y += speed * s * sin(d);
            if (x >= 1 || x <= 0)
                if (d <= PI)
                    d = PI - d;
                else
                    d = (3 * PI) - d;

            if (y >= 1 || y <= 0)
                d = TAU - d;*/
            d = cDir;
            s = cSpeed;
            x += speed * s * cos(d);
            y -= speed * s * sin(d);
        }

        public void draw() {
            fill(70, 200, 20);
            stroke(255);
            ellipse(x * height, y * height, (float) Math.sqrt(w) * weightToRadius * scaling, (float) Math.sqrt(w) * weightToRadius * scaling);
        }

        public void mousePressed() {
            while (true) ;

        }
    }

    public class DumbCell extends Cell {
        public float d, s;

        public DumbCell(float weight) {
            super(weight);
            d = random(TAU);
            s = random(1);
        }

        public void updaten() {
            x += speed * s * cos(d);
            y += speed * s * sin(d);
            if (x >= 1 || x <= 0)
                if (d <= PI)
                    d = PI - d;
                else
                    d = (3 * PI) - d;

            if (y >= 1 || y <= 0)
                d = TAU - d;
        }

        public void draw() {
            fill(204, 102, 0);
            noStroke();
            ellipse(x * height, y * height, (float) Math.sqrt(w) * weightToRadius * scaling, (float) Math.sqrt(w) * weightToRadius * scaling);
        }
    }

    public void settings() {
        fullScreen();
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"NeuralCell"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}

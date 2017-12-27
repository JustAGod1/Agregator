package ru.justagod.agregator.misc;

import javafx.scene.image.Image;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import ru.justagod.agregator.helper.IOHelper;

import java.io.IOException;

public class MaterialBundle {

    private Image specular;
    private Image texture;
    private Image normal;

    public MaterialBundle(String name) {
        try {
            specular = new Image(IOHelper.createInput("image/" + name + "_specular.png"));
        } catch (IOException e) {
            System.out.println("Can't find specular map for " + name);
        }
        try {
            normal = new Image(IOHelper.createInput("image/" + name + "_normal.png"));
        } catch (IOException e) {
            System.out.println("Can't find normal map for " + name);
        }
        try {
            texture = new Image(IOHelper.createInput("image/" + name + ".png"));
        } catch (IOException e) {
            System.out.println("Can't find texture map for " + name);
        }
    }

    public MaterialBundle(Image specular, Image texture, Image normal) {
        this.specular = specular;
        this.texture = texture;
        this.normal = normal;
    }



    public Image getTexture() {
        return texture;
    }

    public void setTexture(Image texture) {
        this.texture = texture;
    }

    public Image getNormal() {
        return normal;
    }

    public void setNormal(Image normal) {
        this.normal = normal;
    }

    public Material createMaterial() {
        PhongMaterial mat = new PhongMaterial();

        mat.setDiffuseMap(texture);
        mat.setSpecularMap(specular);
        mat.setBumpMap(normal);

        return mat;
    }
}

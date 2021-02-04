package com.example.arcoreimagetracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

private lateinit var arFrag: ArFragment
private var viewRenderable: ViewRenderable? = null



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFrag = supportFragmentManager.findFragmentById(R.id.fragArImg)
                as ArFragment


        val renderableFuture = ViewRenderable.builder()
            .setView(this, R.layout.view_renderable)
            .build()
        renderableFuture.thenAccept { viewRenderable = it }
        arFrag.arSceneView.scene.addOnUpdateListener { frameUpdate() }
    }

    private fun frameUpdate() {
        val arFrame = arFrag.arSceneView.arFrame
        if (arFrame != null) {
            if (arFrame.camera.trackingState != TrackingState.TRACKING) return
        }
        arFrame?.getUpdatedTrackables(AugmentedImage::class.java)?.forEach {
            when (it.trackingState) {
                null -> return@forEach
                TrackingState.PAUSED -> {
                    // Image initially detected, but not enough data available to estimate its location in 3D space.
                    // Do not use the image's pose and size estimates until the image's tracking state is tracking
                    val text = "${R.string.detected_img_need_more_info} ${it.name}"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
                TrackingState.STOPPED -> {
                    val text = "${R.string.track_stop} ${it.name}"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
                TrackingState.TRACKING -> {
                    val anchors = it.anchors
                    if (anchors.isEmpty()) {
                        val fitToScanImg = findViewById<ImageView>(R.id.fitToScanImg)
                        fitToScanImg.visibility = View.GONE
                        // Create anchor and anchor node in the center of the image.
                        val pose = it.centerPose
                        val anchor = it.createAnchor(pose)
                        val anchorNode = AnchorNode(anchor)
                        //Attach anchor node in the scene
                        anchorNode.setParent(arFrag.arSceneView.scene)
                        // Create a node as a child node of anchor node, and define node's renderable according to augmented image
                        val imgNode = TransformableNode(arFrag.transformationSystem)
                        imgNode.setParent(anchorNode)
                        viewRenderable?.view?.findViewById<TextView>(R.id.txtImgTrack)?.text =
                            it.name
                        imgNode.renderable = viewRenderable
                        imgNode.setParent(anchorNode)
                        viewRenderable?.view?.findViewById<TextView>(R.id.txtImgTrack)?.text =
                            it.name
                        imgNode.renderable = viewRenderable
                    }

                }
            }
        }
    }
}
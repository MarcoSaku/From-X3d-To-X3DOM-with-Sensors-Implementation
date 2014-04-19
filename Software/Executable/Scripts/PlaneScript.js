
        var cellSize = 1.0;        
        var lastMouseX = -1;
        var lastMouseY = -1;        
        var draggedTransformNode = null;        
        //vectors in 3D world space, associated to mouse x/y movement on the screen
        var draggingUpVec    = null;
        var draggingRightVec = null;        
        var unsnappedDragPos = null;        
		
        var mouseMoved = function(event)
        {
		var x = event.hasOwnProperty('offsetX') ? event.offsetX : event.layerX;
		var y = event.hasOwnProperty('offsetY') ? event.offsetY : event.layerY;
		
            if (lastMouseX === -1)
            {
                lastMouseX = x;
            }
            if (lastMouseY === -1)
            {
                lastMouseY = y;
            }

            if (draggedTransformNode)
            {
               dragObject(x - this.lastMouseX, y - this.lastMouseY);
            }

            lastMouseX = x;
            lastMouseY = y;
        };
        
        //------------------------------------------------------------------------------------------------------------------
        
        var startDragging = function(transformNode)
        {        
            //disable navigation during dragging
            document.getElementById("navInfo").setAttribute("type", '"NONE"');                       
            draggedTransformNode = transformNode;            
            unsnappedDragPos     = new x3dom.fields.SFVec3f.parse(transformNode.getAttribute("translation"));            
            
            //compute the dragging vectors in world coordinates
            //(since navigation is disabled, those will not change until dragging has been finished)

            //get the viewer's 3D local frame
            var x3dElem  = document.getElementById("x3dElement");
            var vMatInv  = x3dElem.runtime.viewMatrix().inverse();            
            var viewDir  = vMatInv.multMatrixVec(new x3dom.fields.SFVec3f(0.0, 0.0, -1.0));
            
            //use the viewer's up-vector and right-vector
            draggingUpVec    = vMatInv.multMatrixVec(new x3dom.fields.SFVec3f(0.0, 1.0,  0.0));;
            draggingRightVec = viewDir.cross(this.draggingUpVec);   
            
            //project a world unit to the screen to get its size in pixels            
            var x3dElem  = document.getElementById("x3dElement");
            var p1 = x3dElem.runtime.calcCanvasPos(unsnappedDragPos.x, unsnappedDragPos.y, unsnappedDragPos.z);
            var p2 = x3dElem.runtime.calcCanvasPos(unsnappedDragPos.x + draggingRightVec.x,
                                                   unsnappedDragPos.y + draggingRightVec.y,
                                                   unsnappedDragPos.z + draggingRightVec.z)
            var magnificationFactor = 1.0 / Math.abs(p1[0] - p2[0]);
            
            //scale up vector and right vector accordingly            
            draggingUpVec    = draggingUpVec.multiply(magnificationFactor);
            draggingRightVec = draggingRightVec.multiply(magnificationFactor);            
        };

        //------------------------------------------------------------------------------------------------------------------

        var dragObject = function(dx, dy)
        {
            //scale up vector and right vector accordingly            
            var offsetUp    = draggingUpVec.multiply(-dy);
            var offsetRight = draggingRightVec.multiply(dx);
			//document.getElementById("mouseOverEvent").innerHTML="Mouse right:"+offsetRight;
            unsnappedDragPos = unsnappedDragPos.add(offsetUp).add(offsetRight);
            var snappedDragPos;
                       
            draggedTransformNode.setAttribute("translation", unsnappedDragPos.toString());
            
        }
        //------------------------------------------------------------------------------------------------------------------

        var stopDragging = function()
        {
            draggedTransformNode = null;                
            draggingUpVec        = null;
            draggingRightVec     = null;
            unsnappedDragPos     = null;            
            //re-enable navigation after dragging
            document.getElementById("navInfo").setAttribute("type", '"EXAMINE" "ANY"');
        };        
    
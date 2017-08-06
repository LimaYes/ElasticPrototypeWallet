package delight.rhinosandox

import RhinoSandboxImpl

class RhinoSandboxes {
	
	def static RhinoSandbox create() {
		return new RhinoSandboxImpl
	}
	
}
JRectangle : JEvent{
	createUnique {
		this.sendMakeCmd("JRectangle");
	}
	test {
		this.create();
		this.setLoc([100, 100]);
		this.setSize([100, 100]);
		this.setColor([255, 255, 255, 255]);
	}
	setQuadColor{
		|a, b, c, d, alpha=1.0|
		a = a.toJV;
		b = b.toJV;
		c = c.toJV;
		d = d.toJV;
		~visualUDP.sendMsg("/setVal", id, "quadColor",
			a[0], a[1], a[2],
			b[0], b[1], b[2],
			c[0], c[1], c[2],
			d[0], d[1], d[2], alpha*255);
	}
	setFilled {
		|value = true|
		~visualUDP.sendMsg("/setVal", id, "fill", value);
	}
}
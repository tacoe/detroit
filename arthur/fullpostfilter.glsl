// TE 2012
// 
// full postprocessing filter
// 'visual monosodium glutamate for retro games'
//
// applies scanlines, crawl, contrast, flicker, 
// vignette, tint, and bloom/glow.
//
// uses parts of Inigo Quilez's 'postpro' shader.

#ifdef GL_ES
precision highp float;
#endif

uniform vec2 resolution;
uniform float time;
uniform sampler2D tex0;

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main(void)
{
    float bloom = 0.35;  // TODO make uniform input (bloom = points)
    float shift = 0.003; // TODO make uniform input (shift = damage)

    vec2 q = gl_FragCoord.xy / resolution.xy;
    vec2 uv = 0.5 + (q-0.5);
    vec3 col;
    vec4 sum = vec4(0);
    vec4 curcol = texture2D(tex0,vec2(q.x,1.0-q.y));

    // neighbourhood values for bloom
    for(int i=-4;i<4;i++) {
        for (int j=-3;j<3;j++) {
            sum += texture2D(tex0,vec2(j,i)*0.004 + vec2(q.x,1.0-q.y)) * 0.25;
        }
      }
          
    // beam shift (plus random distortion)
    if(rand(vec2(1.-time, sin(time)))>0.98) { 
       shift = 0.1*rand(vec2(time, time)); 
    }
    col.r = texture2D(tex0,vec2(uv.x+shift,-uv.y)).x;
    col.g = texture2D(tex0,vec2(uv.x,-uv.y)).y;
    col.b = texture2D(tex0,vec2(uv.x-shift,-uv.y)).z;

    col = clamp(col*0.5+0.5*col*col*1.2,0.0,1.0);          // tone curve
    col *= 0.5 + 0.5*16.0*uv.x*uv.y*(1.0-uv.x)*(1.0-uv.y); // vignette
    col *= vec3(0.7,1.0,0.6);                              // green tint
    col *= 0.8+0.2*sin(10.0*time+uv.y*900.0);              // scanlines
    col *= 1.0-0.05*rand(vec2(time, tan(time)));           // random flicker

    // bloom
    if (curcol.g < 0.3) {
       gl_FragColor = bloom*(sum*sum) * 0.012 + vec4(col,1.0);
    } else {
       if (curcol.g < 0.5) { 
          gl_FragColor = bloom*(sum*sum) * 0.009 + vec4(col,1.0); 
       } else { 
          gl_FragColor = bloom*(sum*sum) * 0.0075 + vec4(col,1.0); 
       }
    }
}
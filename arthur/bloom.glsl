#ifdef GL_ES
precision highp float;
#endif

uniform vec2 resolution;
uniform float time;
uniform sampler2D tex0;

void main()
{
   vec4 sum = vec4(0);
   //vec2 texcoord = vec2(gl_TexCoord[0]);
   //int j;
   //int i;

   vec2 q = gl_FragCoord.xy / resolution.xy;
   vec2 uv = 0.5 + (q-0.5)*(0.9 + 0.1*sin(0.2*time));
   vec4 oricol = texture2D(tex0,vec2(q.x,1.0-q.y));
   vec3 col;

   for(int i=-4;i<4;i++) {
        for (int j=-3;j<3;j++) {
            sum += texture2D(tex0,vec2(j,i)*0.004 + vec2(q.x,1.0-q.y)) * 0.25;
        }
   }
   if (oricol.r < 0.3) {
       gl_FragColor = sum*sum*0.012 + oricol;
   } else {
       if (oricol.r < 0.5) { 
          gl_FragColor = sum*sum*0.009 + oricol; 
       } else { 
          gl_FragColor = sum*sum*0.0075 + oricol; 
       }
   }
}

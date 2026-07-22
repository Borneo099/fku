#!/usr/bin/env python3
"""Convert MCP SRG names (m_XXXXX_, f_XXXXX_) to deobfuscated names using MCP mapping files."""

import re
import os
import csv
import zipfile

def parse_mcp_csv(zip_path, csv_name):
    """Parse MCP methods.csv or fields.csv from the mapping zip."""
    result = {}
    with zipfile.ZipFile(zip_path, 'r') as z:
        with z.open(csv_name) as f:
            reader = csv.reader(line.decode('utf-8') for line in f)
            header = next(reader)  # searge,name,side,desc
            searge_idx = header.index('searge')
            name_idx = header.index('name')
            for row in reader:
                if len(row) >= 3:
                    srg_name = row[searge_idx]  # e.g., m_5661_
                    deobf_name = row[name_idx]   # e.g., displayClientMessage
                    if srg_name.startswith(('m_', 'f_', 'func_', 'field_')):
                        result[srg_name] = deobf_name
    return result

def main():
    mapping_zip = '/root/.gradle/caches/forge_gradle/mcp_repo/net/minecraft/mapping/1.20.1/mapping-1.20.1-mapping.zip'
    source_dir = '/workspace/common/src/main/java'
    
    print(f"Parsing MCP mapping from: {mapping_zip}")
    method_map = parse_mcp_csv(mapping_zip, 'methods.csv')
    field_map = parse_mcp_csv(mapping_zip, 'fields.csv')
    print(f"Loaded {len(method_map)} method mappings and {len(field_map)} field mappings")
    
    # Pattern: m_XXXXX_ or f_XXXXX_ (SRG names)
    srg_pattern = re.compile(r'\b(m_\d+_|f_\d+_)\b')
    
    total_fixed = 0
    for root, dirs, files in os.walk(source_dir):
        for file in files:
            if not file.endswith('.java'):
                continue
            file_path = os.path.join(root, file)
            
            with open(file_path, 'r') as f:
                content = f.read()
            
            original = content
            
            # Find all SRG names in the content
            matches = set(srg_pattern.findall(content))
            
            if not matches:
                continue
            
            # Check which ones exist in the mapping
            replacements = {}
            for name in matches:
                if name in method_map:
                    replacements[name] = method_map[name]
                elif name in field_map:
                    replacements[name] = field_map[name]
            
            if not replacements:
                continue
            
            # Sort by length descending to avoid partial replacements
            for old, new in sorted(replacements.items(), key=lambda x: -len(x[0])):
                content = content.replace(old, new)
            
            if content != original:
                with open(file_path, 'w') as f:
                    f.write(content)
                print(f"  Fixed: {file_path} ({len(replacements)} replacements)")
                total_fixed += 1
    
    print(f"\nTotal files modified: {total_fixed}")

if __name__ == '__main__':
    main()